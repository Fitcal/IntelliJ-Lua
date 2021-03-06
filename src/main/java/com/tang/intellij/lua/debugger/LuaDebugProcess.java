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

package com.tang.intellij.lua.debugger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.commands.DebugCommand;
import com.tang.intellij.lua.debugger.commands.GetStackCommand;
import com.tang.intellij.lua.debugger.mobdebug.MobServer;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebugProcess extends XDebugProcess {

    private LuaDebuggerEditorsProvider editorsProvider;
    private MobServer mobServer;
    private XLineBreakpoint<XBreakpointProperties> breakpoint;

    LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
        current = this;
        editorsProvider = new LuaDebuggerEditorsProvider();
        mobServer = new MobServer(this);
        try {
            mobServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static LuaDebugProcess current;

    public static LuaDebugProcess getCurrent() {
        return current;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void stop() {
        if (mobServer != null) {
            mobServer.stop();
        }
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        mobServer.addCommand("RUN");
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        mobServer.addCommand("OVER");
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        mobServer.addCommand("STEP");
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        mobServer.addCommand("OUT");
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[] { new LuaLineBreakpointHandler(this) };
    }

    void addBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        this.breakpoint = breakpoint;
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null) {
            Project project = getSession().getProject();
            VirtualFile file = sourcePosition.getFile();
            String fileShortUrl = LuaFileUtil.getShortUrl(project, file);
            if (fileShortUrl != null) {
                mobServer.addCommand(String.format("SETB %s %d", fileShortUrl, sourcePosition.getLine() + 1));
            }
        }
    }

    void removeBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null) {
            String shortFilePath = breakpoint.getShortFilePath();
            mobServer.addCommand(String.format("DELB %s %d", shortFilePath, sourcePosition.getLine() + 1));
        }
    }

    public void handleResp(int code, String[] params) {
        switch (code) {
            case 202:
                runCommand(new GetStackCommand(params));
                break;
        }
    }

    public void setStack(LuaExecutionStack stack) {
        ApplicationManager.getApplication().invokeLater(()-> {
            getSession().breakpointReached(breakpoint, null, new LuaSuspendContext(stack));
            getSession().showExecutionPoint();
        });
    }

    public void runCommand(DebugCommand command) {
        mobServer.addCommand(command);
    }
}
