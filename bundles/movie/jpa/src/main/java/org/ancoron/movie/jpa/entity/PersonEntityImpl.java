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

package org.ancoron.movie.jpa.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import org.ancoron.common.model.Gender;
import org.ancoron.common.persistence.AbstractIdentifiable;
import org.ancoron.movie.persistence.model.PersonEntity;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="mov_person")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="c_type", discriminatorType=DiscriminatorType.CHAR)
public abstract class PersonEntityImpl extends AbstractIdentifiable implements PersonEntity, Serializable {
    
    @Enumerated(EnumType.STRING)
    @Column(name="c_gender")
    private Gender gender;

    /**
     * Get the value of gender
     *
     * @return the value of gender
     */
    @Override
    public Gender getGender() {
        return gender;
    }

    /**
     * Set the value of gender
     *
     * @param gender new value of gender
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    private String forename;

    /**
     * Get the value of forename
     *
     * @return the value of forename
     */
    @Override
    public String getForename() {
        return forename;
    }

    /**
     * Set the value of forename
     *
     * @param forename new value of forename
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    private String surname;

    /**
     * Get the value of surname
     *
     * @return the value of surname
     */
    @Override
    public String getSurname() {
        return surname;
    }

    /**
     * Set the value of surname
     *
     * @param surname new value of surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }
}
