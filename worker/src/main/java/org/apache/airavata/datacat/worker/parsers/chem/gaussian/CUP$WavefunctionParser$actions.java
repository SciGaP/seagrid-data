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
package org.apache.airavata.datacat.worker.parsers.chem.gaussian;

import java.util.Stack;

public /** Cup generated class to encapsulate user supplied action code.*/
class CUP$WavefunctionParser$actions {


    //__________________________________
    public static boolean DEBUG = false;


    private final WavefunctionParser parser;

    /** Constructor */
    CUP$WavefunctionParser$actions(WavefunctionParser parser) {
        this.parser = parser;
    }

    /** Method with the actual generated action code. */
    public final java_cup.runtime.Symbol CUP$WavefunctionParser$do_action(
            int                        CUP$WavefunctionParser$act_num,
            java_cup.runtime.lr_parser CUP$WavefunctionParser$parser,
            Stack CUP$WavefunctionParser$stack,
            int                        CUP$WavefunctionParser$top)
            throws Exception
    {
      /* Symbol object for return from actions */
        java_cup.runtime.Symbol CUP$WavefunctionParser$result;

      /* select the action based on the action number */
        switch (CUP$WavefunctionParser$act_num)
        {
          /*. . . . . . . . . . . . . . . . . . . .*/
            case 2: // scfintro ::= FOUNDITER RUNTYP
            {
                Object RESULT = null;
                if (DEBUG) System.out.println("CUP:WFparser: gaussian:  found FOUNDITER ");
                CUP$WavefunctionParser$result = new java_cup.runtime.Symbol(2/*scfintro*/, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-1)).left, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-0)).right, RESULT);
            }
            return CUP$WavefunctionParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 1: // $START ::= startpt EOF
            {
                Object RESULT = null;
                int start_valleft = ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-1)).left;
                int start_valright = ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-1)).right;
                Object start_val = (Object)((java_cup.runtime.Symbol) CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-1)).value;
                RESULT = start_val;
                CUP$WavefunctionParser$result = new java_cup.runtime.Symbol(0/*$START*/, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-1)).left, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-0)).right, RESULT);
            }
          /* ACCEPT */
            CUP$WavefunctionParser$parser.done_parsing();
            return CUP$WavefunctionParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
            case 0: // startpt ::= scfintro
            {
                Object RESULT = null;
                if (DEBUG) System.out.println("CUP:WFParser: gaussian:  end of parse tree ");


                CUP$WavefunctionParser$result = new java_cup.runtime.Symbol(1/*startpt*/, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-0)).left, ((java_cup.runtime.Symbol)CUP$WavefunctionParser$stack.elementAt(CUP$WavefunctionParser$top-0)).right, RESULT);
            }
            return CUP$WavefunctionParser$result;

          /* . . . . . .*/
            default:
                throw new Exception(
                        "Invalid action number found in internal parse table");

        }
    }
}
