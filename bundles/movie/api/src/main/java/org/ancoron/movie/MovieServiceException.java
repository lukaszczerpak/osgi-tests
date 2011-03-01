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

package org.ancoron.movie;

/**
 *
 * @author ancoron
 */
public class MovieServiceException extends Exception {

    /**
     * Creates a new instance of <code>MovieServiceException</code> without detail message.
     */
    public MovieServiceException() {
    }

    /**
     * Constructs an instance of <code>MovieServiceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MovieServiceException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance of <code>MovieServiceException</code> with the specified cause.
     * @param cause the real cause of this exception.
     */
    public MovieServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>MovieServiceException</code> with the specified detail message and cause.
     * @param msg the detail message.
     * @param cause the real cause of this exception.
     */
    public MovieServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
