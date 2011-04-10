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

import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;


/**
 *
 * @author ancoron
 */
@javax.faces.bean.ManagedBean(name = "managedBean")
@RequestScoped
public class ManagedBean
{

    private String name = "Bob";

    @ManagedProperty(value = "#{sessionBean}")
    private JSFSessionBean sessionBean;

    public JSFSessionBean getSessionBean()
    {
        return sessionBean;
    }

    public void setSessionBean(JSFSessionBean sessionBean)
    {
        this.sessionBean = sessionBean;
    }

    /** Creates a new instance of ManagedBean */
    public ManagedBean() {
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
