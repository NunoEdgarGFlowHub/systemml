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

package org.apache.sysml.test.integration.functions.codegenalg;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.apache.sysml.api.DMLScript;
import org.apache.sysml.api.DMLScript.RUNTIME_PLATFORM;
import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.lops.LopProperties.ExecType;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.integration.TestConfiguration;
import org.apache.sysml.test.utils.TestUtils;

public class AlgorithmStepwiseRegression extends AutomatedTestBase 
{
	private final static String TEST_NAME1 = "Algorithm_Stepwise";
	private final static String TEST_DIR = "functions/codegenalg/";
	private final static String TEST_CLASS_DIR = TEST_DIR + AlgorithmStepwiseRegression.class.getSimpleName() + "/";
	private final static String TEST_CONF = "SystemML-config-codegen.xml";
	private final static File   TEST_CONF_FILE = new File(SCRIPT_DIR + TEST_DIR, TEST_CONF);
	
	private final static int rows = 2468;
	private final static int cols = 200;
	
	private final static double sparsity1 = 0.7; //dense
	private final static double sparsity2 = 0.1; //sparse
	
	private final static int icpt = 0;
	private final static double thr = 0.01;
	
	public enum StepwiseType {
		GLM_PROBIT,
		LINREG_DS,
	}
	
	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
		addTestConfiguration(TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[] { "w" })); 
	}

	@Test
	public void testStepwiseGLMDenseRewritesCP() {
		runStepwiseTest(StepwiseType.GLM_PROBIT, false, true, ExecType.CP);
	}
	
	@Test
	public void testStepwiseGLMSparseRewritesCP() {
		runStepwiseTest(StepwiseType.GLM_PROBIT, true, true, ExecType.CP);
	}
	
	@Test
	public void testStepwiseGLMDenseNoRewritesCP() {
		runStepwiseTest(StepwiseType.GLM_PROBIT, false, false, ExecType.CP);
	}
	
	@Test
	public void testStepwiseGLMSparseNoRewritesCP() {
		runStepwiseTest(StepwiseType.GLM_PROBIT, true, false, ExecType.CP);
	}
	
//	@Test
//	public void testStepwiseGLMDenseRewritesSP() {
//		runStepwiseTest(StepwiseType.GLM_PROBIT, false, true, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseGLMSparseRewritesSP() {
//		runStepwiseTest(StepwiseType.GLM_PROBIT, true, true, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseGLMDenseNoRewritesSP() {
//		runStepwiseTest(StepwiseType.GLM_PROBIT, false, false, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseGLMSparseNoRewritesSP() {
//		runStepwiseTest(StepwiseType.GLM_PROBIT, true, false, ExecType.SPARK);
//	}
	
	@Test
	public void testStepwiseLinregDSDenseRewritesCP() {
		runStepwiseTest(StepwiseType.LINREG_DS, false, true, ExecType.CP);
	}
	
	@Test
	public void testStepwiseLinregDSSparseRewritesCP() {
		runStepwiseTest(StepwiseType.LINREG_DS, true, true, ExecType.CP);
	}
	
	@Test
	public void testStepwiseLinregDSDenseNoRewritesCP() {
		runStepwiseTest(StepwiseType.LINREG_DS, false, false, ExecType.CP);
	}
	
	@Test
	public void testStepwiseLinregDSSparseNoRewritesCP() {
		runStepwiseTest(StepwiseType.LINREG_DS, true, false, ExecType.CP);
	}
	
//	@Test
//	public void testStepwiseLinregDSDenseRewritesSP() {
//		runStepwiseTest(StepwiseType.LINREG_DS, false, true, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseLinregDSSparseRewritesSP() {
//		runStepwiseTest(StepwiseType.LINREG_DS, true, true, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseLinregDSDenseNoRewritesSP() {
//		runStepwiseTest(StepwiseType.LINREG_DS, false, false, ExecType.SPARK);
//	}
//	
//	@Test
//	public void testStepwiseLinregDSSparseNoRewritesSP() {
//		runStepwiseTest(StepwiseType.LINREG_DS, true, false, ExecType.SPARK);
//	}
	
	private void runStepwiseTest( StepwiseType type, boolean sparse, boolean rewrites, ExecType instType)
	{
		boolean oldFlag = OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION;
		RUNTIME_PLATFORM platformOld = rtplatform;
		switch( instType ){
			case SPARK: rtplatform = RUNTIME_PLATFORM.SPARK; break;
			default: rtplatform = RUNTIME_PLATFORM.HYBRID_SPARK; break;
		}
	
		boolean sparkConfigOld = DMLScript.USE_LOCAL_SPARK_CONFIG;
		if( rtplatform == RUNTIME_PLATFORM.SPARK || rtplatform == RUNTIME_PLATFORM.HYBRID_SPARK )
			DMLScript.USE_LOCAL_SPARK_CONFIG = true;

		try
		{
			String TEST_NAME = TEST_NAME1;
			TestConfiguration config = getTestConfiguration(TEST_NAME);
			loadTestConfiguration(config);
			
			if( type ==  StepwiseType.LINREG_DS) {
				fullDMLScriptName = "scripts/algorithms/StepLinearRegDS.dml";
				programArgs = new String[]{ "-explain", "-stats", "-nvargs",
					"X="+input("X"), "Y="+input("Y"), "icpt="+String.valueOf(icpt),
					"thr="+String.valueOf(thr), "B="+output("B"), "S="+output("S")};
			}
			else { //GLM binomial probit
				fullDMLScriptName = "scripts/algorithms/StepGLM.dml";
				programArgs = new String[]{ "-explain", "-stats", "-nvargs",
					"X="+input("X"), "Y="+input("Y"), "icpt="+String.valueOf(icpt),
					"thr="+String.valueOf(thr), "link=3", "yneg=0",
					"moi=5", "mii=5", "B="+output("B"), "S="+output("S")};
			}
			
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = rewrites;
			
			//generate actual datasets
			double[][] X = getRandomMatrix(rows, cols, 0, 1, sparse?sparsity2:sparsity1, 714);
			writeInputMatrixWithMTD("X", X, true);
			double[][] y = TestUtils.round(getRandomMatrix(rows, 1, 0, 1, 1.0, 136));
			writeInputMatrixWithMTD("Y", y, true);
			
			runTest(true, false, null, -1); 

			Assert.assertTrue(heavyHittersContainsSubString("spoof")
				|| heavyHittersContainsSubString("sp_spoof"));
		}
		finally {
			rtplatform = platformOld;
			DMLScript.USE_LOCAL_SPARK_CONFIG = sparkConfigOld;
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = oldFlag;
			OptimizerUtils.ALLOW_AUTO_VECTORIZATION = true;
			OptimizerUtils.ALLOW_OPERATOR_FUSION = true;
		}
	}

	/**
	 * Override default configuration with custom test configuration to ensure
	 * scratch space and local temporary directory locations are also updated.
	 */
	@Override
	protected File getConfigTemplateFile() {
		// Instrumentation in this test's output log to show custom configuration file used for template.
		System.out.println("This test case overrides default configuration with " + TEST_CONF_FILE.getPath());
		return TEST_CONF_FILE;
	}
}
