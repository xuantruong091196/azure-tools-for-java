/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.toolkit.lib.appservice.file;

import com.google.common.base.Joiner;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.rest.RestClient;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AppServiceFileService {

    private final AppServiceFileClient client;
    private final WebAppBase app;

    private AppServiceFileService(final WebAppBase app, AppServiceFileClient client) {
        this.app = app;
        this.client = client;
    }

    public List<? extends AppServiceFile> getFilesInDirectory(String dir) {
        // this file is generated by kudu itself, should not be visible to user.
        final Predicate<AppServiceFile> filter = file -> !"text/xml".equals(file.getMime()) || !file.getName().contains("LogFiles-kudu-trace_pending.xml");
        final List<AppServiceFile> files = this.client.getFilesInDirectory(dir).toBlocking().first().stream().filter(filter).collect(Collectors.toList());
        files.forEach(file -> {
            file.setApp(this.app);
            file.setPath(dir + "/" + file.getName());
        });
        return files;
    }

    public Observable<byte[]> getFileContent(final String path) {
        return this.client.getFileContent(path).flatMap((Func1<ResponseBody, Observable<byte[]>>) responseBody -> {
            final BufferedSource source = responseBody.source();
            return Observable.create((Action1<Emitter<byte[]>>) emitter -> {
                try {
                    while (!source.exhausted()) {
                        emitter.onNext(source.readByteArray());
                    }
                    emitter.onCompleted();
                } catch (final IOException e) {
                    emitter.onError(e);
                }
            }, Emitter.BackpressureMode.BUFFER);
        });
    }

    public static AppServiceFileService forApp(final WebAppBase app) {
        final AppServiceFileClient client = getClient(app);
        return new AppServiceFileService(app, client);
    }

    private static AppServiceFileClient getClient(WebAppBase app) {
        if (app.defaultHostName() == null) {
            throw new UnsupportedOperationException("Cannot initialize kudu vfs client before web app is created");
        } else {
            String host = app.defaultHostName().toLowerCase().replace("http://", "").replace("https://", "");
            final String[] parts = host.split("\\.", 2);
            host = Joiner.on('.').join(parts[0], "scm", parts[1]);
            final AppServiceManager manager = app.manager();
            final RestClient restClient = getRestClient(manager);
            return restClient.newBuilder()
                             .withBaseUrl("https://" + host)
                             .withConnectionTimeout(3L, TimeUnit.MINUTES)
                             .withReadTimeout(3L, TimeUnit.MINUTES)
                             .build()
                             .retrofit()
                             .create(KuduFileClient.class);
        }
    }

    @SneakyThrows
    private static RestClient getRestClient(final AppServiceManager manager) {
        //TODO: @wangmi find a proper way to get rest client.
        final Method method = manager.getClass().getDeclaredMethod("restClient");
        method.setAccessible(true);
        return (RestClient) method.invoke(manager);
    }
}
