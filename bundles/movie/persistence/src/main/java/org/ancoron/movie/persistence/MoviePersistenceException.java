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

package org.ancoron.movie.persistence;

/**
 *
 * @author ancoron
 */
public class MoviePersistenceException extends Exception {

    /**
     * Creates a new instance of <code>MoviePersistenceException</code> without detail message.
     */
    public MoviePersistenceException() {
    }


    /**
     * Constructs an instance of <code>MoviePersistenceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MoviePersistenceException(String msg) {
        super(msg);
    }

    public MoviePersistenceException(Throwable cause) {
        super(cause);
    }

    public MoviePersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
