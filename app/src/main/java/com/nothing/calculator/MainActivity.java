package com.nothing.calculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;
    private StringBuilder expression = new StringBuilder();
    private boolean justEvaluated = false;
    private boolean isDegMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        setupButtons();
    }

    private void setupButtons() {
        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9};
        String[] nums = {"0","1","2","3","4","5","6","7","8","9"};
        for (int i = 0; i < numIds.length; i++) {
            final String num = nums[i];
            findViewById(numIds[i]).setOnClickListener(v -> appendToExpr(num));
        }

        findViewById(R.id.btnPlus).setOnClickListener(v -> appendToExpr("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> appendToExpr("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendToExpr("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendToExpr("÷"));
        findViewById(R.id.btnDot).setOnClickListener(v -> appendDot());
        findViewById(R.id.btnOpenParen).setOnClickListener(v -> appendToExpr("("));
        findViewById(R.id.btnCloseParen).setOnClickListener(v -> appendToExpr(")"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> appendToExpr("%"));
        findViewById(R.id.btnSin).setOnClickListener(v -> appendToExpr("sin("));
        findViewById(R.id.btnCos).setOnClickListener(v -> appendToExpr("cos("));
        findViewById(R.id.btnTan).setOnClickListener(v -> appendToExpr("tan("));
        findViewById(R.id.btnLog).setOnClickListener(v -> appendToExpr("log("));
        findViewById(R.id.btnLn).setOnClickListener(v -> appendToExpr("ln("));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> appendToExpr("√("));
        findViewById(R.id.btnPow2).setOnClickListener(v -> appendToExpr("²"));
        findViewById(R.id.btnPow).setOnClickListener(v -> appendToExpr("^"));
        findViewById(R.id.btnPi).setOnClickListener(v -> appendToExpr("π"));
        findViewById(R.id.btnE).setOnClickListener(v -> appendToExpr("e"));
        findViewById(R.id.btnInv).setOnClickListener(v -> invertExpr());
        findViewById(R.id.btnNegate).setOnClickListener(v -> negateExpr());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> backspace());
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());

        Button btnRad = findViewById(R.id.btnRad);
        Button btnDeg = findViewById(R.id.btnDeg);
        btnRad.setOnClickListener(v -> { isDegMode = false; btnRad.setAlpha(1f); btnDeg.setAlpha(0.4f); });
        btnDeg.setOnClickListener(v -> { isDegMode = true; btnDeg.setAlpha(1f); btnRad.setAlpha(0.4f); });
    }

    private void appendToExpr(String s) {
        if (justEvaluated && s.matches("[0-9π]")) { expression.setLength(0); }
        justEvaluated = false;
        expression.append(s);
        tvResult.setText(expression.toString());
        tvExpression.setText("");
    }

    private void appendDot() {
        if (justEvaluated) { expression.setLength(0); justEvaluated = false; }
        String expr = expression.toString();
        int i = expr.length() - 1;
        while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) i--;
        if (!expr.substring(i + 1).contains(".")) expression.append(".");
        tvResult.setText(expression.toString());
    }

    private void invertExpr() {
        if (expression.length() == 0) return;
        String wrapped = "(1/(" + expression + "))";
        expression.setLength(0);
        expression.append(wrapped);
        tvResult.setText(expression.toString());
    }

    private void negateExpr() {
        if (expression.length() == 0) return;
        String s = expression.toString();
        expression.setLength(0);
        if (s.startsWith("-(") && s.endsWith(")")) expression.append(s.substring(2, s.length()-1));
        else expression.append("-(").append(s).append(")");
        tvResult.setText(expression.toString());
    }

    private void clearAll() {
        expression.setLength(0);
        justEvaluated = false;
        tvResult.setText("0");
        tvExpression.setText("");
    }

    private void backspace() {
        if (expression.length() > 0) expression.deleteCharAt(expression.length()-1);
        tvResult.setText(expression.length() == 0 ? "0" : expression.toString());
        justEvaluated = false;
    }

    private void calculate() {
        if (expression.length() == 0) return;
        String exprStr = expression.toString();
        tvExpression.setText(exprStr + " =");
        try {
            double result = evaluate(exprStr);
            String resultStr;
            if (result == Math.floor(result) && !Double.isInfinite(result) && Math.abs(result) < 1e15)
                resultStr = String.valueOf((long) result);
            else resultStr = String.format("%.6g", result);
            tvResult.setText(resultStr);
            expression.setLength(0);
            expression.append(resultStr);
            justEvaluated = true;
        } catch (Exception ex) {
            tvResult.setText("Error");
            expression.setLength(0);
        }
    }

    private double evaluate(String expr) {
        expr = expr.replace("×","*").replace("÷","/")
                .replace("π", String.valueOf(Math.PI))
                .replace("²","^2").replace("%","/100");
        return parseExpr(new ExprParser(expr));
    }

    private double parseExpr(ExprParser p) {
        double r = parseTerm(p);
        while (p.hasMore() && (p.peek()=='+' || p.peek()=='-')) {
            char op = p.consume();
            r = op=='+' ? r+parseTerm(p) : r-parseTerm(p);
        }
        return r;
    }

    private double parseTerm(ExprParser p) {
        double r = parsePower(p);
        while (p.hasMore() && (p.peek()=='*' || p.peek()=='/')) {
            char op = p.consume();
            r = op=='*' ? r*parsePower(p) : r/parsePower(p);
        }
        return r;
    }

    private double parsePower(ExprParser p) {
        double base = parseUnary(p);
        if (p.hasMore() && p.peek()=='^') { p.consume(); return Math.pow(base, parsePower(p)); }
        return base;
    }

    private double parseUnary(ExprParser p) {
        if (p.hasMore() && p.peek()=='-') { p.consume(); return -parseUnary(p); }
        if (p.hasMore() && p.peek()=='+') { p.consume(); return parseUnary(p); }
        return parsePrimary(p);
    }

    private double parsePrimary(ExprParser p) {
        if (p.hasMore() && (Character.isDigit(p.peek()) || p.peek()=='.')) return parseNumber(p);
        if (p.hasMore() && p.peek()=='(') {
            p.consume(); double v = parseExpr(p);
            if (p.hasMore() && p.peek()==')') p.consume();
            return v;
        }
        if (p.startsWith("sin(")) { p.skip(4); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.sin(isDegMode?Math.toRadians(a):a); }
        if (p.startsWith("cos(")) { p.skip(4); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.cos(isDegMode?Math.toRadians(a):a); }
        if (p.startsWith("tan(")) { p.skip(4); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.tan(isDegMode?Math.toRadians(a):a); }
        if (p.startsWith("log(")) { p.skip(4); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.log10(a); }
        if (p.startsWith("ln("))  { p.skip(3); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.log(a); }
        if (p.startsWith("√("))  { p.skip(2); double a=parseExpr(p); if(p.hasMore()&&p.peek()==')')p.consume(); return Math.sqrt(a); }
        if (p.startsWith("e"))   { p.skip(1); return Math.E; }
        throw new RuntimeException("Unexpected");
    }

    private double parseNumber(ExprParser p) {
        StringBuilder sb = new StringBuilder();
        while (p.hasMore() && (Character.isDigit(p.peek()) || p.peek()=='.')) sb.append(p.consume());
        return Double.parseDouble(sb.toString());
    }

    static class ExprParser {
        final String s; int pos=0;
        ExprParser(String s) { this.s=s.replaceAll("\\s+",""); }
        boolean hasMore() { return pos<s.length(); }
        char peek() { return s.charAt(pos); }
        char consume() { return s.charAt(pos++); }
        void skip(int n) { pos+=n; }
        boolean startsWith(String prefix) { return s.startsWith(prefix,pos); }
    }
}
