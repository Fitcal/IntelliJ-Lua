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

package com.tang.intellij.lua.codeInsight.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.tang.intellij.lua.psi.LuaExpr;
import com.tang.intellij.lua.psi.LuaVar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LuaPostfixUtils {

    public static PostfixTemplateExpressionSelector selectorTopmost() {
        return selectorTopmost(Conditions.alwaysTrue());
    }

    public static PostfixTemplateExpressionSelector selectorTopmost(Condition<PsiElement> additionalFilter) {
        return new PostfixTemplateExpressionSelectorBase(additionalFilter) {
            @Override
            protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement psiElement, @NotNull Document document, int i) {
                PsiElement expr = PsiTreeUtil.getParentOfType(psiElement, LuaExpr.class);
                if (expr == null) {
                    expr = PsiTreeUtil.getParentOfType(psiElement, LuaVar.class);
                }
                /*if (expr == null) {
                    if (psiElement.getNode().getElementType() == LuaTypes.NUMBER) {
                        expr = psiElement;
                    }
                }*/
                return ContainerUtil.createMaybeSingletonList(expr);
            }
        };
    }
}
