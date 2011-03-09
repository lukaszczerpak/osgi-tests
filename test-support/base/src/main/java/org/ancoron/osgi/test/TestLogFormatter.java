/*
 * Copyright 2011 ancoron.
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

package org.ancoron.osgi.test;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author ancoron
 */
public class TestLogFormatter extends Formatter {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss.SSS Z']'", Locale.ENGLISH);
    
    private static final int LVL_FINEST = 300;
    private static final int LVL_FINER = 400;
    private static final int LVL_FINE = 500;
    private static final int LVL_CONFIG = 700;
    private static final int LVL_INFO = 800;
    private static final int LVL_WARNING = 900;
    private static final int LVL_SEVERE = 1000;

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(dateFormat.format(new Date(record.getMillis())));
        
        final Level level = record.getLevel();
        switch(level.intValue()) {
            case LVL_INFO:
            case LVL_FINE:
                sb.append("[   ");
                break;
            case LVL_FINER:
                sb.append("[  ");
                break;
            case LVL_FINEST:
            case LVL_CONFIG:
            case LVL_SEVERE:
                sb.append("[ ");
                break;
            default:
                break;
        }

        sb.append(level.getName()).append("] ");
        
        final Object[] params = record.getParameters();
        if(params != null && params.length > 0) {
            sb.append(MessageFormat.format(record.getMessage(), params));
        } else {
            sb.append(record.getMessage());
        }
        sb.append("\n");
        
        return sb.toString();
    }

}
