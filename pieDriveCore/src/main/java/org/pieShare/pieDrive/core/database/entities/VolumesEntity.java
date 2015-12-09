package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Roland on 08.12.2015.
 */
@Entity
public class VolumesEntity implements IBaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String volumeName;



    //TODO change to enum
    private String raidLevel;

    public Long getId(){ return id; }

    public void setId(Long id) { this.id = id; }

    public String getVolumeName() { return volumeName; }

    public void setVolumeName(String volumeName) { this.volumeName = volumeName; }

    public String getRaidLevel() { return raidLevel; }

    public void setRaidLevel(String raidLevel) { this.raidLevel = raidLevel; }

}
