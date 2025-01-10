package com.letmedevelop

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class MultiFilePastePathAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        val aggregatedContent = StringBuilder()

        for (file in selectedFiles) {
            val psiFile = getPsiFile(project, file) ?: continue
            val relativePath = getRelativePath(project, file)

            val fileContent = addPathToTopIfNeeded(project, psiFile, relativePath)
            aggregatedContent.append(fileContent).append("\n")
        }

        copyToClipboard(aggregatedContent.toString())
    }

    private fun getPsiFile(project: Project, file: VirtualFile): PsiFile? {
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)
        return psiManager.findFile(file)
    }

    private fun addPathToTopIfNeeded(project: Project, psiFile: PsiFile, path: String): String {
        val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(psiFile.virtualFile)
        val existingContent = psiFile.text
        val comment = getCommentForFile(psiFile, path)

        if (!existingContent.startsWith(comment)) {
            WriteCommandAction.runWriteCommandAction(project) {
                document?.insertString(0, "$comment\n")
            }
            return "$comment\n$existingContent"
        }

        return existingContent
    }

    private fun getRelativePath(project: Project, virtualFile: VirtualFile): String {
        return VfsUtilCore.getRelativePath(virtualFile, project.baseDir) ?: ""
    }

    private fun getCommentForFile(psiFile: PsiFile, path: String): String {
        val fileName = psiFile.name.lowercase()
        return when {
            psiFile.fileType.defaultExtension.lowercase() in listOf(
                "js", "jsx", "ts", "tsx", "java", "kt", "cpp", "c", "h", "hpp", "cs", "swift", "go", "rs",
                "py", "rb", "pl", "sh", "bash", "yaml", "yml", "properties", "gradle", "sql", "lua"
            ) -> "// $path"
            psiFile.fileType.defaultExtension.lowercase() in listOf("css", "scss", "less", "json") -> "/* $path */"
            psiFile.fileType.defaultExtension.lowercase() in listOf("html", "xml") -> "<!-- $path -->"
            psiFile.fileType.defaultExtension.lowercase() == "php" -> if (psiFile.text.contains("<?php")) "// $path" else "<!-- $path -->"
            psiFile.fileType.defaultExtension.lowercase() == "md" -> "<!-- $path -->"
            psiFile.fileType.defaultExtension.lowercase() == "txt" -> "# $path"
            psiFile.fileType.defaultExtension.lowercase() == "bat" -> "REM $path"
            psiFile.fileType.defaultExtension.lowercase() == "ini" -> "; $path"
            fileName.endsWith(".blade.php") -> "{{-- $path --}}"
            psiFile.fileType.defaultExtension.lowercase() in listOf("asp", "aspx") -> "<!-- $path -->"
            else -> "" // Default for unknown types
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(text)
        clipboard.setContents(stringSelection, null)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) != null
    }
}
