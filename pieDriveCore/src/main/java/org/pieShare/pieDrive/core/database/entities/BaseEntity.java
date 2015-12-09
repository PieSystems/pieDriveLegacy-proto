/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.entities;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author richy
 */
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(
            strategy
            = GenerationType.AUTO)
    protected Long id;

    public Long getId() {
        return id;
    }
}
