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
package org.ancoron.movie.web;

import java.io.Serializable;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author ancoron
 */
@javax.faces.bean.ManagedBean(name = "sessionBean")
@SessionScoped
public class JSFSessionBean implements Serializable
{
    /** Creates a new instance of JSFSessionBean */
    public JSFSessionBean() {
    }

    private String sessionValue;

    /**
     * Get the value of sessionValue
     *
     * @return the value of sessionValue
     */
    public String getSessionValue()
    {
        return sessionValue;
    }

    /**
     * Set the value of sessionValue
     *
     * @param sessionValue new value of sessionValue
     */
    public void setSessionValue(String sessionValue)
    {
        this.sessionValue = sessionValue;
    }
}
