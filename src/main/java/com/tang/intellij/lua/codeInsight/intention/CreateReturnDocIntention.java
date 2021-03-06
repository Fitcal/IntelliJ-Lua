/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaFuncBody;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class CreateReturnDocIntention extends ClassMethodBasedIntention {
    @Override
    protected boolean isAvailable(LuaClassMethodDef methodDef, Editor editor) {
        LuaComment comment = methodDef.getComment();
        return comment == null || PsiTreeUtil.getChildrenOfType(comment, LuaDocReturnDef.class) != null;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @NotNull
    @Override
    public String getText() {
        return "Create return declaration";
    }

    @Override
    protected void invoke(LuaClassMethodDef methodDef, Editor editor) {
        LuaComment comment = methodDef.getComment();
        LuaFuncBody funcBody = methodDef.getFuncBody();
        if (funcBody != null) {
            TemplateManager templateManager = TemplateManager.getInstance(methodDef.getProject());
            Template template = templateManager.createTemplate("", "");
            if (comment != null) template.addTextSegment("\n");
            template.addTextSegment("---@return ");
            MacroCallNode typeSuggest = new MacroCallNode(new SuggestTypeMacro());
            template.addVariable("returnType", typeSuggest, new TextExpression("table"), false);
            template.addEndVariable();
            if (comment != null) {
                editor.getCaretModel().moveToOffset(comment.getTextOffset() + comment.getTextLength());
            } else {
                template.addTextSegment("\n");
                editor.getCaretModel().moveToOffset(methodDef.getNode().getStartOffset());
            }
            templateManager.startTemplate(editor, template);
        }
    }
}
