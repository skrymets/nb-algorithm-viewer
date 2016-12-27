/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.sandbox;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import org.netbeans.module.sandbox.model.alg.SourceCodeBoundaries;

/**
 *
 * @author Lot
 */
public class SourceCodeHelper {

    private final CompilationUnitTree cut;
    private final SourcePositions sourcePositions;

    public SourceCodeHelper(CompilationUnitTree cut, SourcePositions sourcePositions) {
        this.cut = cut;
        this.sourcePositions = sourcePositions;
    }

    public SourceCodeBoundaries getSourceCodeBoundaries(Tree node) {

        LineMap lineMap = cut.getLineMap();
        long startLine = lineMap.getLineNumber(sourcePositions.getStartPosition(cut, node));
        long endLine = lineMap.getLineNumber(sourcePositions.getEndPosition(cut, node));

        return new SourceCodeBoundaries(startLine, endLine);
    }

}
