/*
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
 */

package org.apache.sysml.runtime.instructions.cp;

import org.apache.sysml.runtime.DMLRuntimeException;
import org.apache.sysml.runtime.controlprogram.context.ExecutionContext;
import org.apache.sysml.runtime.matrix.data.LibCommonsMath;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.runtime.matrix.operators.Operator;
import org.apache.sysml.runtime.matrix.operators.UnaryOperator;

public class UnaryMatrixCPInstruction extends UnaryCPInstruction {
	protected UnaryMatrixCPInstruction(Operator op, CPOperand in, CPOperand out, String opcode, String instr) {
		super(CPType.Unary, op, in, out, opcode, instr);
	}

	@Override 
	public void processInstruction(ExecutionContext ec) 
		throws DMLRuntimeException 
	{	
		String output_name = output.getName();
		
		String opcode = getOpcode();
		if(LibCommonsMath.isSupportedUnaryOperation(opcode)) {
			MatrixBlock retBlock = LibCommonsMath.unaryOperations(ec.getMatrixObject(input1.getName()),getOpcode());
			ec.setMatrixOutput(output_name, retBlock, getExtendedOpcode());
		}
		else {
			UnaryOperator u_op = (UnaryOperator) _optr;
			MatrixBlock inBlock = ec.getMatrixInput(input1.getName(), getExtendedOpcode());
			MatrixBlock retBlock = (MatrixBlock) (inBlock.unaryOperations(u_op, new MatrixBlock()));
		
			ec.releaseMatrixInput(input1.getName(), getExtendedOpcode());
			
			// Ensure right dense/sparse output representation (guarded by released input memory)
			if( checkGuardedRepresentationChange(inBlock, retBlock) ) {
	 			retBlock.examSparsity();
	 		}
			
			ec.setMatrixOutput(output_name, retBlock, getExtendedOpcode());
		}
	}
}
