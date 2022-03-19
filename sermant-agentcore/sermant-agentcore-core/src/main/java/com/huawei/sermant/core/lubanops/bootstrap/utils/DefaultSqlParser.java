/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.lubanops.bootstrap.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public class DefaultSqlParser implements SqlParser {

    public static final char SEPARATOR = ',';

    public static final char SYMBOL_REPLACE = '$';

    public static final char NUMBER_REPLACE = '#';

    private static final int NEXT_TOKEN_NOT_EXIST = -1;

    private static final int NORMALIZED_SQL_BUFFER = 32;

    private static final NormalizedSql NULL_OBJECT = new DefaultNormalizedSql("", "");

    private static final Set NORMAL_CHAR = new HashSet(
            Arrays.asList(' ', '\t', '\n', '\r', '*', '+', '%', '=', '<', '>', '&', '|', '^', '~', '!', '(', ')', ',',
                    ';'));
    private static final Set SEC_CHAR = new HashSet(
            Arrays.asList('.', '_', '@', ':'));

    @Override
    public NormalizedSql normalizedSql(String sql) {
        sql = sql.toLowerCase(Locale.getDefault()).trim();
        if (sql == null) {
            return NULL_OBJECT;
        }

        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length + NORMALIZED_SQL_BUFFER);
        final StringBuilder parsedParameter = new StringBuilder(32);
        boolean change = false;
        int replaceIndex = 0;
        boolean numberTokenStartEnable = true;
        // boolean numberTokenStartEnable = false;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            // COMMENT start check
            if (ch == '/') {
                // comment state
                i = processVirgule(sql, length, normalized, i, ch);
            } else if (ch == '-') {
                if (lookAhead1(sql, i) == '-') {
                    normalized.append("--");
                    i += 2;
                    i = readLine(sql, normalized, i);
                } else {
                    // unary operator
                    numberTokenStartEnable = true;
                    normalized.append(ch);
                }
            } else if (ch == '\'') {
                i = processBlackSlash(sql, length, normalized, parsedParameter, i);
            } else if (ch >= '0' && ch <= '9') {
                i = process9(sql, length, normalized, parsedParameter, numberTokenStartEnable, i, ch);
            } else if (NORMAL_CHAR.contains(ch)) {
                numberTokenStartEnable = true;
                normalized.append(ch);
            } else if (SEC_CHAR.contains(ch)) {
                numberTokenStartEnable = false;
                normalized.append(ch);
            } else {
                if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                    numberTokenStartEnable = false;
                } else {
                    numberTokenStartEnable = true;
                }
                normalized.append(ch);
            }
        }
        return getDefaultNormalizedSql(normalized, parsedParameter);
    }

    private DefaultNormalizedSql getDefaultNormalizedSql(StringBuilder normalized, StringBuilder parsedParameter) {
        String parsedParameterString;
        if (parsedParameter.length() > 0) {
            parsedParameterString = parsedParameter.toString();
        } else {
            parsedParameterString = "";
        }
        // Change here
        String[] resArray = normalized.toString().split(",");
        StringBuilder res = new StringBuilder();
        for (String ele : resArray) {
            String eleTrim = ele.trim();
            if (eleTrim.length() == 1) {
                if (!eleTrim.equals("?")) {
                    res.append(eleTrim);
                }
                continue;
            }
            Character sec = eleTrim.charAt(1);
            if (eleTrim.charAt(0) == '?') {
                res.append(eleTrim.substring(1));
            } else {
                res.append(",");
                res.append(eleTrim);
            }
        }

        return new DefaultNormalizedSql(res.toString().substring(1, res.length()), parsedParameterString);
    }

    private int process9(String sql, int length, StringBuilder normalized, StringBuilder parsedParameter,
            boolean numberTokenStartEnable, int i, char ch) {
        boolean change;
        if (numberTokenStartEnable) {
            change = true;
            normalized.append('?');
            // number token start
            appendOutputSeparator(parsedParameter);
            appendOutputParam(parsedParameter, ch);
            i++;
            tokenEnd: for (; i < length; i++) {
                char stateCh = sql.charAt(i);
                if (stateCh == '0' || stateCh == '1' || stateCh == '2' || stateCh == '3' || stateCh == '4'
                        || stateCh == '5' || stateCh == '6' || stateCh == '7' || stateCh == '8' || stateCh == '9'
                        || stateCh == '.' || stateCh == 'E' || stateCh == 'e') {
                    appendOutputParam(parsedParameter, stateCh);
                } else {
                    i--;
                    break tokenEnd;
                }
            }
            return i;
        } else {
            normalized.append(ch);
            return i;
        }
    }

    private int processBlackSlash(String sql, int length, StringBuilder normalized, StringBuilder parsedParameter,
            int i) {
        boolean change;
        if (lookAhead1(sql, i) == '\'') {
            normalized.append("''");
            // no need to add parameter to output as $ is not converted
            i += 2;
        } else {
            normalized.append('?');
            i++;
            appendOutputSeparator(parsedParameter);
            for (; i < length; i++) {
                char stateCh = sql.charAt(i);
                if (stateCh == '\'') {
                    // a consecutive ' is the same as \'
                    if (lookAhead1(sql, i) == '\'') {
                        i++;
                        appendOutputParam(parsedParameter, "''");
                        continue;
                    } else {
                        break;
                    }
                }
                appendSeparatorCheckOutputParam(parsedParameter, stateCh);
            }
        }
        return i;
    }

    private int processVirgule(String sql, int length, StringBuilder normalized, int i, char ch) {
        final int lookAhead1Char = lookAhead1(sql, i);
        // multi line comment and oracle hint /*+ */
        if (lookAhead1Char == '*') {
            normalized.append("/*");
            i += 2;
            for (; i < length; i++) {
                char stateCh = sql.charAt(i);
                if (stateCh == '*') {
                    if (lookAhead1(sql, i) == '/') {
                        normalized.append("*/");
                        i++;
                        break;
                    }
                }
                normalized.append(stateCh);
            }
        } else if (lookAhead1Char == '/') {
            normalized.append("//");
            i += 2;
            i = readLine(sql, normalized, i);

        } else {
            // unary operator
            normalized.append(ch);
        }
        return i;
    }

    private int readLine(String sql, StringBuilder normalized, int index) {
        final int length = sql.length();
        for (; index < length; index++) {
            char ch = sql.charAt(index);
            normalized.append(ch);
            if (ch == '\n') {
                break;
            }
        }
        return index;
    }

    private void appendOutputSeparator(StringBuilder output) {
        if (output.length() == 0) {
            // first parameter
            return;
        }
        output.append(SEPARATOR);
    }

    private void appendOutputParam(StringBuilder output, String str) {
        output.append(str);
    }

    private void appendSeparatorCheckOutputParam(StringBuilder output, char ch) {
        if (ch == ',') {
            output.append(",,");
        } else {
            output.append(ch);
        }
    }

    private void appendOutputParam(StringBuilder output, char ch) {
        output.append(ch);
    }

    /**
     * look up the next character in a string
     * @param sql
     * @param index
     * @return
     */
    private int lookAhead1(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    @Override
    public String combineOutputParams(String sql, List<String> outputParams) {

        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);

            if (ch == '/') {
                i = processVirgule(sql, length, normalized, i, ch);
            } else if (ch == '-') {
                // single line comment state
                if (lookAhead1(sql, i) == '-') {
                    normalized.append("--");
                    i += 2;
                    i = readLine(sql, normalized, i);
                    break;
                } else {
                    // unary operator
                    normalized.append(ch);
                    break;
                }
            } else if (ch >= '0' && ch <= '9') {
                i = processCombine9(sql, outputParams, length, normalized, i, ch);
            } else {
                normalized.append(ch);
            }
        }

        return normalized.toString();
    }

    private int processCombine9(String sql, List<String> outputParams, int length, StringBuilder normalized, int i,
            char ch) {
        if (lookAhead1(sql, i) == NEXT_TOKEN_NOT_EXIST) {
            normalized.append(ch);
            return i;
        }
        StringBuilder outputIndex = new StringBuilder();
        outputIndex.append(ch);
        // number token start
        i++;
        tokenEnd: for (; i < length; i++) {
            final char stateCh = sql.charAt(i);
            switch (stateCh) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (lookAhead1(sql, i) == NEXT_TOKEN_NOT_EXIST) {
                        outputIndex.append(stateCh);
                        normalized.append(outputIndex.toString());
                        break tokenEnd;
                    }
                    outputIndex.append(stateCh);
                    break;
                case NUMBER_REPLACE:
                    numberReplace(outputIndex, normalized, outputParams);
                    break tokenEnd;
    
                case SYMBOL_REPLACE:
                    symbolReplace(outputIndex, normalized, outputParams);
                    break tokenEnd;
    
                default:
                    normalized.append(outputIndex.toString());
                    i--;
                    break tokenEnd;
            }
        }
        return i;
    }
    
    private void numberReplace(StringBuilder outputIndex, StringBuilder normalized, List<String> outputParams) {
        int numberIndex = 0;
        try {
            numberIndex = Integer.parseInt(outputIndex.toString());
        } catch (NumberFormatException e) {
            normalized.append(outputIndex.toString());
            normalized.append(NUMBER_REPLACE);
            return;
        }
        try {
            String replaceNumber = outputParams.get(numberIndex);
            normalized.append(replaceNumber);
        } catch (IndexOutOfBoundsException e) {
            normalized.append(outputIndex.toString());
            normalized.append(NUMBER_REPLACE);
            return;
        }
    }

    private void symbolReplace(StringBuilder outputIndex, StringBuilder normalized, List<String> outputParams) {
        int symbolIndex = 0;
        try {
            symbolIndex = Integer.parseInt(outputIndex.toString());
        } catch (NumberFormatException e) {
            // just append for invalid parameters
            normalized.append(outputIndex.toString());
            normalized.append(SYMBOL_REPLACE);
        }
        try {
            String replaceSymbol = outputParams.get(symbolIndex);
            normalized.append(replaceSymbol);
        } catch (IndexOutOfBoundsException e) {
            normalized.append(outputIndex.toString());
            normalized.append(SYMBOL_REPLACE);
        }
    }

    private Queue<String> getBindValueQueue(List<String> bindValues) {
        final Queue<String> bindValueQueue = new LinkedList<String>();
        for (String value : bindValues) {
            // trim
            bindValueQueue.add(value.trim());
        }
        return bindValueQueue;
    }

    @Override
    public String combineBindValues(String sql, List<String> bindValues) {
        if (StringUtils.isBlank(sql) || bindValues.isEmpty()) {
            return sql;
        }

        final Queue<String> bindValueQueue = getBindValueQueue(bindValues);
        final int length = sql.length();
        final StringBuilder result = new StringBuilder(length + 16);

        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            if (inQuotes) {
                if (((ch == '\'') || (ch == '"')) && ch == quoteChar) {
                    if (lookAhead1(sql, i) == quoteChar) {
                        // inline quote.
                        result.append(ch);
                        i++;
                        continue;
                    }
                    inQuotes = !inQuotes;
                    quoteChar = 0;
                }
                result.append(ch);
            } else {
                // COMMENT start check
                if (ch == '/') {
                    i = processVirgule(sql, length, result, i, ch);
                } else if (ch == '-') {
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        result.append("--");
                        i += 2;
                        i = readLine(sql, result, i);
                    } else {
                        // unary operator
                        result.append(ch);
                    }
                } else if (ch == '\'' || ch == '"') {
                    inQuotes = true;
                    quoteChar = ch;
                    result.append(ch);
                } else if (ch == '?') {
                    if (!bindValueQueue.isEmpty()) {
                        result.append('\'').append(bindValueQueue.poll()).append('\'');
                    }
                } else {
                    result.append(ch);
                }
            }
        }

        return result.toString();
    }

}