/*
 *
 */
package com.microsoft.intellij.helpers.spring;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.intellij.helpers.UIHelperImpl;
import org.jetbrains.annotations.NotNull;

public class SpringCloudAppPropertyViewProvider implements FileEditorProvider, DumbAware {

    public static final String SPRING_CLOUD_APP_PROPERTY_TYPE = "SPRING_CLOUD_APP_PROPERTY";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType().getName().equals(getEditorTypeId());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final String sid = virtualFile.getUserData(UIHelperImpl.SUBSCRIPTION_ID);
        final String resourceGroup = virtualFile.getUserData(UIHelperImpl.RESOURCE_GROUP_NAME);
        final String cluster = virtualFile.getUserData(UIHelperImpl.CLUSTER_NAME);
        final String appName = virtualFile.getUserData(UIHelperImpl.APP_NAME);
        return new SpringCloudAppPropertyView(sid, resourceGroup, cluster, appName);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return SPRING_CLOUD_APP_PROPERTY_TYPE;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
