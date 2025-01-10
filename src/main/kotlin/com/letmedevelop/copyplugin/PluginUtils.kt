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

class PastePathAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val virtualFile = psiFile.virtualFile ?: return

        val relativePath = getRelativePath(project, virtualFile)
        val comment = getCommentForFile(psiFile, relativePath)

        val fileText = psiFile.text

        val firstLine = fileText.lines().firstOrNull()?.trim()
        val isPathAlreadyPresent = firstLine == comment

        if (!isPathAlreadyPresent) {
            // Paste the comment at the top if not already present
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.insertString(0, "$comment\n")
            }
        }

        // Prepare the clipboard content (comment + file content or original file content)
        val clipboardContent = if (isPathAlreadyPresent) fileText else "$comment\n$fileText"

        // Copy the content to clipboard
        copyToClipboard(clipboardContent)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && e.getData(CommonDataKeys.EDITOR) != null
    }

    private fun getRelativePath(project: Project, virtualFile: VirtualFile): String {
        return VfsUtilCore.getRelativePath(virtualFile, project.baseDir) ?: ""
    }

    private fun getCommentForFile(psiFile: PsiFile, path: String): String {
        val fileName = psiFile.name.lowercase()
        return when {
            psiFile.fileType.defaultExtension.lowercase() in listOf("js", "jsx", "ts", "tsx", "java", "kt", "cpp", "c", "h", "hpp", "cs", "swift", "go", "rs") -> "// $path"
            psiFile.fileType.defaultExtension.lowercase() in listOf("css", "scss", "less") -> "/* $path */"
            psiFile.fileType.defaultExtension.lowercase() in listOf("html", "xml") -> "<!-- $path -->"
            psiFile.fileType.defaultExtension.lowercase() in listOf("py", "rb", "pl", "sh", "bash", "yaml", "yml", "properties") -> "# $path"
            psiFile.fileType.defaultExtension.lowercase() == "php" -> {
                if (psiFile.text.contains("<?php")) {
                    "// $path"
                } else {
                    "<!-- $path -->"
                }
            }
            psiFile.fileType.defaultExtension.lowercase() in listOf("sql", "lua") -> "-- $path"
            psiFile.fileType.defaultExtension.lowercase() == "json" -> "/* $path */"
            psiFile.fileType.defaultExtension.lowercase() == "gradle" -> "// $path"
            psiFile.fileType.defaultExtension.lowercase() == "md" -> "<!-- $path -->"
            psiFile.fileType.defaultExtension.lowercase() == "txt" -> "# $path"
            psiFile.fileType.defaultExtension.lowercase() == "bat" -> "REM $path"
            psiFile.fileType.defaultExtension.lowercase() == "ini" -> "; $path"
            fileName.endsWith(".blade.php") -> "{{-- $path --}}" // Blade templates
            psiFile.fileType.defaultExtension.lowercase() in listOf("asp", "aspx") -> "<!-- $path -->" // ASP.NET
            else -> "" // Default to no comment
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(text)
        clipboard.setContents(stringSelection, null)
    }
}
