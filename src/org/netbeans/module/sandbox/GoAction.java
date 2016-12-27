/*
 * Copyright 2016 Lot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.module.sandbox;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.Phase.*;
import org.netbeans.editor.BaseAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Lot
 */
@ActionID(
        category = "File",
        id = "org.netbeans.grasp4.sandbox.ui.GoAction")
@ActionRegistration(
        iconBase = "org/netbeans/module/sandbox/media/icon.png",
        displayName = "#CTL_SandboxGoAction")
@ActionReference(
        path = "Toolbars/File",
        position = 0)
@Messages("CTL_SandboxGoAction=Go!")
//public class GoAction implements ActionListener {
public class GoAction extends BaseAction {

    private static final long serialVersionUID = -3573611070054305384L;

    private static final InputOutput IO = IOProvider.getDefault().getIO("Sandbox", false);

    private final DataObject context;

    public GoAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ae, JTextComponent textComponent) {

        FileObject fileObject = context.getPrimaryFile();
        if (fileObject == null) {
            return;
        }

        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            StatusDisplayer.getDefault().setStatusText("Not a Java file: " + fileObject.getPath());
        } else {

            try {

                javaSource.runUserActionTask((CompilationController parameter) -> {
                    parameter.toPhase(ELEMENTS_RESOLVED);
                    new CodeFlowRepresentationTaskFactory().createTask(fileObject).run(parameter);
                }, true);
            } catch (IOException ex) {
                // StatusDisplayer.getDefault().setStatusText(ex.getLocalizedMessage());
                IO.getErr().append(ex.getLocalizedMessage());
                IO.getErr().flush();
            } finally {
            }

        }
    }

}
