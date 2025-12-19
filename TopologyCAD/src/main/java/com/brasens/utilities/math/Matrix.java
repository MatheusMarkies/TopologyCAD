package com.brasens.utilities.math;

/**
 * 12-08-2020
 * @author Matheus Markies
 */
public class Matrix {

    public static double[][] identity(int length) {
        double[][] I = new double[length][length];
        for (int i = 0; i < I.length; i++) {
            for (int j = 0; j < I[0].length; j++) {
                if (j == i) {
                    I[i][j] = 1;
                } else {
                    I[i][j] = 0;
                }
            }
        }
        return I;
    }
    public static double[][] identity(double[][] A) {
        double[][] I = new double[A.length][A[0].length];
        for (int i = 0; i < I.length; i++) {
            for (int j = 0; j < I[0].length; j++) {
                if (j == i) {
                    I[i][j] = 1;
                } else {
                    I[i][j] = 0;
                }
            }
        }
        return I;
    }

    public static double[][] zero(double[][] A) {
        double[][] I = new double[A.length][A[0].length];
        for (int i = 0; i < I.length; i++)
            for (int j = 0; j < I[0].length; j++)
                I[i][j] = 0;

        return I;
    }

    public static double[][] one(double[][] A) {
        double[][] I = new double[A.length][A[0].length];
        for (int i = 0; i < I.length; i++)
            for (int j = 0; j < I[0].length; j++)
                I[i][j] = 1;

        return I;
    }

    public static double[][] transpose(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    public static double[][] multiply(double[][] A, double[][] B) {
        double[][] newMatrix = new double[A.length][B[0].length];

        int n1 = A[0].length;
        int m2 = B.length;
        if (n1 != m2) {
            throw new IllegalArgumentException("Matrices cannot be multiplied: " + n1 + " != " + m2);
        }

        for (int i = 0; i < newMatrix[0].length; i++) { // Linha i
            for (int j = 0; j < newMatrix.length; j++) { // Coluna j

                double a = 0;
                for (int k = 0; k < B.length; k++) {
                    a += A[j][k] * B[k][i];
                }

                newMatrix[j][i] = a;
            }
        }

        return newMatrix;
    }

    public static String[][] multiply(double[][] A, String[][] B) {

        String[][] newMatrix = new String[A.length][B[0].length];

        int n1 = A[0].length;
        int m2 = B.length;
        if (n1 != m2) {
            throw new IllegalArgumentException("Matrices cannot be multiplied: " + n1 + " != " + m2);
        }

        for (int i = 0; i < B[0].length; i++) { // Linha i
            for (int j = 0; j < A.length; j++) { // Coluna j

                StringBuilder a = new StringBuilder();
                for (int k = 0; k < B.length; k++) {
                    if (a.length() > 0) {
                        a.append(" + ").append(A[j][k]).append(" * ").append(B[k][i]);
                    } else {
                        a.append(A[j][k]).append(" * ").append(B[k][i]);
                    }
                }

                newMatrix[j][i] = a.toString();
            }
        }

        return newMatrix;
    }

    public static double[] multiply(double[][] A, double[] b) {
        int m = A.length;
        int n = A[0].length;

        int n1 = A[0].length;
        int m2 = b.length;
        if (n1 != m2) {
            throw new IllegalArgumentException("Matrices cannot be multiplied: " + n1 + " != " + m2);
        }

        double[] c = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                c[i] += A[i][j] * b[j];
            }
        }
        return c;
    }
    public static double[][] multiplyMatrix(double[] vector, double[][] matrix) {
        int m = matrix.length;    // number of rows in matrix
        int n = matrix[0].length; // number of columns in matrix
        int p = vector.length;    // length of vector

        if (m != p) {
            throw new IllegalArgumentException("Matrix rows must match vector length");
        }

        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = vector[i] * matrix[i][j];
            }
        }

        return result;
    }

    public static double[][] multiplyMatrix(double[][] matrix, double[] vector) {
        int m = matrix.length;    // number of rows in matrix
        int n = matrix[0].length; // number of columns in matrix
        int p = vector.length;    // length of vector

        if (n != p) {
            throw new IllegalArgumentException("Matrix columns must match vector length");
        }

        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix[i][j] * vector[j];
            }
        }

        return result;
    }


    public static double[] multiply(double[] array, double[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        if (n != array.length) {
            throw new IllegalArgumentException("Array length must be equal to matrix column length");
        }
        double[] result = new double[m];
        for (int i = 0; i < m; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += matrix[i][j] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    public static double[] add(double[] array1, double[] array2) {
        int n = array1.length;
        if (n != array2.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = array1[i] + array2[i];
        }
        return result;
    }

    public static double[] subtract(double[] array1, double[] array2) {
        int n = array1.length;
        if (n != array2.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = array1[i] - array2[i];
        }
        return result;
    }

    public static double[][] add(double[][] matrix1, double[][] matrix2) {
        int m = matrix1.length;
        int n = matrix1[0].length;
        if (m != matrix2.length || n != matrix2[0].length) {
            throw new IllegalArgumentException("Matrices must have the same dimensions");
        }
        double[][] result = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    public static double[][] subtract(double[][] matrix1, double[][] matrix2) {
        int m = matrix1.length;
        int n = matrix1[0].length;
        if (m != matrix2.length || n != matrix2[0].length) {
            throw new IllegalArgumentException("Matrices must have the same dimensions");
        }
        double[][] result = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    public static double[][] add(double[][] matrix, double[] array) {
        int m = matrix.length;
        int n = matrix[0].length;
        if (n != array.length) {
            throw new IllegalArgumentException("Array length must be equal to matrix column length");
        }
        double[][] result = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix[i][j] + array[j];
            }
        }
        return result;
    }

    public static double[][] subtract(double[][] matrix, double[] array) {
        int m = matrix.length;
        int n = matrix[0].length;
        if (n != array.length) {
            throw new IllegalArgumentException("Array length must be equal to matrix column length");
        }
        double[][] result = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix[i][j] - array[j];
            }
        }
        return result;
    }


    public static String toString(double[][] A) {
        StringBuilder matrix = new StringBuilder();
        for (double[] doubles : A) {
            for (int j = 0; j < A[0].length; j++) {
                matrix.append(doubles[j]).append(" ");
            }
            matrix.append("\n");
        }
        return matrix.toString();
    }

    public static double[][] invese(double[][] a) {
        int n = a.length;
        double[][] x = new double[n][n];
        double[][] b = new double[n][n];
        int[] index = new int[n];
        for (int i = 0; i < n; ++i) {
            b[i][i] = 1;
        }

        gaussian(a, index);

        for (int i = 0; i < n - 1; ++i) {
            for (int j = i + 1; j < n; ++j) {
                for (int k = 0; k < n; ++k) {
                    b[index[j]][k]
                            -= a[index[j]][i] * b[index[i]][k];
                }
            }
        }

        for (int i = 0; i < n; ++i) {
            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) {
                    x[j][i] -= a[index[j]][k] * x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }

    public static void gaussian(double[][] a, int[] index) {
        int n = index.length;
        double[] c = new double[n];

        for (int i = 0; i < n; ++i) {
            index[i] = i;
        }

        for (int i = 0; i < n; ++i) {
            double c1 = 0;
            for (int j = 0; j < n; ++j) {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) {
                    c1 = c0;
                }
            }
            c[i] = c1;
        }

        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            double pi1 = 0;
            for (int i = j; i < n; ++i) {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }

            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                double pj = a[index[i]][j] / a[index[j]][j];

                a[index[i]][j] = pj;

                for (int l = j + 1; l < n; ++l) {
                    a[index[i]][l] -= pj * a[index[j]][l];
                }
            }
        }
    }

}