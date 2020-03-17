package ToxPredictor.Application.GUI.Miscellaneous;

import Jama.*;

// this version uses the JAMA open source matrix package
public class ConfidenceInterval2 {

	double[] t05 = { 0, 6.314, 2.92, 2.353, 2.132, 2.015, 1.943, 1.895, 1.86,
			1.833, 1.812, 1.796, 1.782, 1.771, 1.761, 1.753, 1.746, 1.74,
			1.734, 1.729, 1.725, 1.721, 1.717, 1.714, 1.711, 1.708, 1.706,
			1.703, 1.701, 1.699, 1.697, 1.684, 1.671, 1.658, 1.645 };

	double[] t025 = { 0, 12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365,
			2.306, 2.262, 2.228, 2.201, 2.179, 2.16, 2.145, 2.131, 2.12, 2.11,
			2.101, 2.093, 2.086, 2.08, 2.074, 2.069, 2.064, 2.06, 2.056, 2.052,
			2.048, 2.045, 2.042, 2.021, 2, 1.98, 1.96 };

	// descriptor values for compounds in model set:
	double[][] X = { { 1, 1.63669, 2.1297 }, { 1, 0.871949, 1.83057 },
			{ 1, 0.387543, 1.39128 }, { 1, 1.08043, 1.68382 },
			{ 1, 1.46518, 0.216832 }, { 1, 1.30502, 0.0315228 },
			{ 1, 0.988792, 0.556235 }, { 1, 1.08043, 1.66567 },
			{ 1, 0.890004, 2.35458 } };

	// experimental tox values for compounds in model set:
	double[][] Y = {

	{ 2.12927 }, { 2.95768 }, { 4.10768 }, { 3.15631 }, { 1.29611 },
			{ 1.74967 }, { 2.39474 }, { 3.06916 }, { 3.62859 } };

	// descriptors for compound to be predicted:
	double[][] x0 = { { 1 }, { 0.651537 }, { 0.5455 } }; // NP+1 x 1 matrix
															// (excluding zero
															// rows&cols)

	double CalculateMeanSquareDeviation(int N, int NP, Matrix Y, Matrix Ycalc) {
		double MSD = 0;
		double Resid = 0;

		for (int i = 0; i <= N - 1; i++) {
			Resid = Y.get(i, 0) - Ycalc.get(i, 0);
			MSD += Resid * Resid;
		}
		MSD /= (N - NP);
		return MSD;

	}

	double Interpolate(double X1, double Y1, double X2, double Y2, double X3) {
		double I = 0;// interpolated value

		I = (X3 - X1) * (Y2 - Y1) / (X2 - X1) + Y1;

		return I;

	}

	void CalculateConfidenceInterval() {
		// TODO: possibly get t-test values from
		// http://jakarta.apache.org/commons/math/

		int N = 9; // number of chemicals in cluster
		int NP = 3; // number of parameters
		double MSD, CI90, CI95;
		double Tstat90 = 0, Tstat95 = 0;

		double[] Ycalc = new double[N];
		double[] Yexp = new double[N];

		Matrix Xm = new Matrix(X);
		Matrix Ym = new Matrix(Y);

		System.out.println(Xm.getRowDimension());
		System.out.println(Xm.getColumnDimension());
		System.out.println(Ym.getRowDimension());
		System.out.println(Ym.getColumnDimension());

		Matrix XVECLSm = Xm.transpose().times(Xm).inverse()
				.times(Xm.transpose()).times(Ym);

		Matrix Ycalcm = Xm.times(XVECLSm);

		// Xm.print(6,4);
		System.out.println("XVECLS=");
		XVECLSm.print(6, 4);

		MSD = this.CalculateMeanSquareDeviation(N, NP, Ym, Ycalcm);
		// System.out.println("MSD="+MSD);

		Matrix x0m = new Matrix(x0);
		System.out.println("x0m=");
		x0m.print(6, 4);

		Matrix bob = x0m.transpose().times(Xm.transpose().times(Xm).inverse());
		// bob=this.addScalar(bob,1);
		bob = bob.times(x0m);

		int DOF = N - NP; // dont need to subtract 1 since constant is included
							// in NP

		Tstat90 = this.GetTStatistic(DOF, 90);
		Tstat95 = this.GetTStatistic(DOF, 95);

		System.out.println("DOF=" + DOF);
		System.out.println("Tstat90=" + Tstat90);
		System.out.println("Tstat95=" + Tstat95);

		// CI90=Tstat90*Math.sqrt(MSD*bob.get(0,0));
		// CI95=Tstat95*Math.sqrt(MSD*bob.get(0,0));

		CI90 = Tstat90 * Math.sqrt(MSD * (1 + bob.get(0, 0)));
		CI95 = Tstat95 * Math.sqrt(MSD * (1 + bob.get(0, 0)));

		System.out.println("CI90 = " + CI90);
		System.out.println("CI95 = " + CI95);

		for (int i = 0; i <= N - 1; i++) {
			Yexp[i] = Ym.get(i, 0);
			Ycalc[i] = Ycalcm.get(i, 0);
		}

		fraChart fc = new fraChart();

		fc.jlChart.Y1 = Ycalc;
		fc.jlChart.X = Yexp;

		fc.setVisible(true);

	}

	public double GetTStatistic(int DOF, int Percentile) {

		double[] tarray;

		if (Percentile == 90)
			tarray = this.t05;
		else
			tarray = this.t025;

		double t = 0;

		if (DOF <= 30) {
			t = tarray[DOF];
		} else if (DOF > 30 && DOF <= 40) {
			t = this.Interpolate(30, tarray[30], 40, tarray[31], DOF);
		} else if (DOF > 40 && DOF <= 60) {
			t = this.Interpolate(40, tarray[31], 60, tarray[32], DOF);
		} else if (DOF > 60 && DOF <= 120) {
			t = this.Interpolate(60, tarray[32], 120, tarray[33], DOF);
		} else if (DOF > 120) {
			t = tarray[34];
		}

		return t;

	}

	public Matrix addScalar(Matrix matrix, double value) {
		int m = matrix.getRowDimension();
		int n = matrix.getColumnDimension();

		double[][] A = matrix.getArray();

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] + value;
			}
		}
		return X;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfidenceInterval2 ci = new ConfidenceInterval2();
		ci.CalculateConfidenceInterval();

	}

}
