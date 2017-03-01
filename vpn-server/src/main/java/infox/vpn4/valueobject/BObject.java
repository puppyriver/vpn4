/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package infox.vpn4.valueobject;

//import com.alcatelsbell.nms.common.crud.annotation.BField;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author g
 */
@MappedSuperclass
public abstract class BObject implements Serializable, Cloneable {
     /**
	 * 
	 */
	private static final long serialVersionUID = -1187811066494970339L;


	@Id
    @GeneratedValue (strategy = GenerationType.TABLE, generator = "ID_GEN")
    @TableGenerator (name = "ID_GEN", table = "ID_GEN2", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "NETELEMENT_GEN", initialValue = 10000, allocationSize = 10000)
  //  @BField(description = "流水号",sequence = 0,createType = BField.CreateType.HIDE,editType = BField.EditType.HIDE,viewType = BField.ViewType.SHOW)
    protected Long id;


      @Column(unique = true, nullable = false)
      protected String dn;

    private String tagsync;
    private String tag1;
    private String tag2;
    private String tag3;

    @Transient
    protected int oid;
    
    @Temporal (TemporalType.TIMESTAMP)
//    @Transient
    protected Date createDate;

    @Temporal (TemporalType.TIMESTAMP)
//    @Transient

    protected Date updateDate = new Date();

//    @Transient
    protected int fromWhere;

    @Transient
    protected Object userObject = null;

    //@Version
    protected Long version;

    public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOid() {
        return oid;
    }

    public void setOid(int _oid) {
        this.oid = _oid;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public int getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(int fromWhere) {
        this.fromWhere = fromWhere;
    }

    public Object getUserObject() {
        return userObject;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getTag1() {
        return tag1;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }
}
