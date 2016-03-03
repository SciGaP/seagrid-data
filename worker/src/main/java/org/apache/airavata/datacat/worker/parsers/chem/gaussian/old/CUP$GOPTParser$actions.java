/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.datacat.worker.parsers.chem.gaussian.old;

import org.apache.airavata.datacat.worker.parsers.chem.gaussian.DefaultGaussianParser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

/**
 * Cup generated class to encapsulate user supplied action code.
 */
public class CUP$GOPTParser$actions {


    //__________________________________
    public static boolean DEBUG = false;
    public ParseGSCF2 temp1;
    public ParseGSCF2 temp2;
    public ParseGSCF2 temp3;
    private static JTable table;
    private static final String tableLabel = "SCF Intermediate Results:";


    public static JTable getTable() {
        return table;
    }

    public static String getTableLabel() {
        return tableLabel;
    }


    private final GOPTParser parser;

    /**
     * Constructor
     */
    CUP$GOPTParser$actions(GOPTParser parser) throws IOException {
        this.parser = parser;
        temp1 = new ParseGSCF2(System.getProperty("java.io.tmpdir") + File.separator + DefaultGaussianParser.randomNum + "Energy_data");
        temp2 = new ParseGSCF2(System.getProperty("java.io.tmpdir") + File.separator + DefaultGaussianParser.randomNum + "temporary2");
        temp3 = new ParseGSCF2(System.getProperty("java.io.tmpdir") + File.separator + DefaultGaussianParser.randomNum + "temporary3");

        temp1.putField("TitleText: Energy versus Iteration" + "\n");
        temp1.putField("XLabel: Iteration\n YLabel: Energy\n");
        temp1.putField("DataSet: Energy\n");
        temp1.putField("1, ");
        temp2.putField("TitleText: Gradient versus Iteration" + "\n");
        temp2.putField("XLabel: Iteration\n YLabel: Maximum Gradient\n");
        temp2.putField("DataSet: Maximum Gradient\n");
        temp3.putField("\n\n");
        temp3.putField("XLabel: Iteration\n YLabel: RMS Gradient\n");
        temp3.putField("DataSet: RMS Gradient\n");
    }

    /**
     * Method with the actual generated action code.
     */
    public final java_cup.runtime.Symbol CUP$GOPTParser$do_action(
            int CUP$GOPTParser$act_num,
            java_cup.runtime.lr_parser CUP$GOPTParser$parser,
            Stack CUP$GOPTParser$stack,
            int CUP$GOPTParser$top)
            throws Exception {
      /* Symbol object for return from actions */
        java_cup.runtime.Symbol CUP$GOPTParser$result;

      /* select the action based on the action number */
        switch (CUP$GOPTParser$act_num) {
          /*. . . . . . . . . . . . . . . . . . . .*/
            case 11: // grad2 ::= RmsGrad RGRAD
            {
                Object RESULT = null;
                int rgleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left;
                int rgright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right;
                Float rg = (Float) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).value;
                //___________________________________________________________________
                if (DEBUG) System.out.println("CUP:gopt: RMS Force " + rg);
                float rms = rg.floatValue();
                temp3.putField(rms);


                CUP$GOPTParser$result = new java_cup.runtime.Symbol(7/*grad2*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 10: // grad1 ::= MaxGrad MGRAD
            {
                Object RESULT = null;
                int mgleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left;
                int mgright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right;
                Float mg = (Float) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).value;
                //___________________________________________________________________
                if (DEBUG) System.out.println("CUP:gopt: Maximum Force " + mg);
                float maxim = mg.floatValue();
                temp2.putField(maxim);

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(6/*grad1*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 9: // cycle ::= grad2
            {
                Object RESULT = null;
                // propagate RESULT from NT$1
                if (((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 2)).value != null)
                    RESULT = (Object) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 2)).value;
                int cleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).left;
                int cright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).right;
                Integer c = (Integer) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).value;

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(5/*cycle*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 8: // cycle ::= grad1
            {
                Object RESULT = null;
                // propagate RESULT from NT$1
                if (((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 2)).value != null)
                    RESULT = (Object) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 2)).value;
                int cleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).left;
                int cright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).right;
                Integer c = (Integer) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 3)).value;

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(5/*cycle*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 7: // cycle ::= NSearch ITERATION
            {
                Object RESULT = null;
                int cleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left;
                int cright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right;
                Integer c = (Integer) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).value;
                //___________________________________________________________________
                if (DEBUG) System.out.println("CUP:gopt:  ITERATION " + c);
                int cycl = c.intValue();
                temp1.putField(cycl + 1);
                temp1.putField(", ");
                temp2.putField(cycl);
                temp2.putField(", ");
                temp3.putField(cycl);
                temp3.putField(", ");
                CUP$GOPTParser$result = new java_cup.runtime.Symbol(5/*cycle*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 6: // scfcycle ::= cycle
            {
                Object RESULT = null;

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(4/*scfcycle*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 5: // scfcycle ::= Energ ENERGY
            {
                Object RESULT = null;
                int eleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left;
                int eright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right;
                Float e = (Float) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).value;
                //___________________________________________________________________
                if (DEBUG) System.out.println("CUP:gopt:  ENERGY " + e);
                float energ = e.floatValue();
                temp1.putField(energ);
                CUP$GOPTParser$result = new java_cup.runtime.Symbol(4/*scfcycle*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 4: // scfpat ::= scfcycle
            {
                Object RESULT = null;

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(3/*scfpat*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 3: // scfpat ::= scfpat scfcycle
            {
                Object RESULT = null;
                if (DEBUG) System.out.println("CUP:gopt: in scfpat");
                CUP$GOPTParser$result = new java_cup.runtime.Symbol(3/*scfpat*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 2: // scfintro ::= FOUNDITER
            {
                Object RESULT = null;
                if (DEBUG) System.out.println("CUP:gopt:  found the start of Iteration");
                CUP$GOPTParser$result = new java_cup.runtime.Symbol(2/*scfintro*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 1: // $START ::= startpt EOF
            {
                Object RESULT = null;
                int start_valleft = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left;
                int start_valright = ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).right;
                Object start_val = (Object) ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).value;
                RESULT = start_val;
                CUP$GOPTParser$result = new java_cup.runtime.Symbol(0/*$START*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 1)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
          /* ACCEPT */
            CUP$GOPTParser$parser.done_parsing();
            return CUP$GOPTParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 0: // startpt ::= scfintro scfpat SCFDONE
            {
                Object RESULT = null;
                if (DEBUG) System.out.println("CUP:gopt:  end of parse tree ");
                table = new JTable();

//       table = parseSCF.getTable();

                CUP$GOPTParser$result = new java_cup.runtime.Symbol(1/*startpt*/, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 2)).left, ((java_cup.runtime.Symbol) CUP$GOPTParser$stack.elementAt(CUP$GOPTParser$top - 0)).right, RESULT);
            }
            return CUP$GOPTParser$result;

          /* . . . . . .*/
            default:
                throw new Exception(
                        "Invalid action number found in internal parse table");

        }
    }
}