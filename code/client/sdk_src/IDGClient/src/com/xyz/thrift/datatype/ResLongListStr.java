/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.xyz.thrift.datatype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.thrift.EncodingUtils;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (1.0.0-dev)", date = "2015-11-23")
public class ResLongListStr implements org.apache.thrift.TBase<ResLongListStr, ResLongListStr._Fields>, java.io.Serializable, Cloneable, Comparable<ResLongListStr> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ResLongListStr");

  private static final org.apache.thrift.protocol.TField RES_FIELD_DESC = new org.apache.thrift.protocol.TField("res", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField VALUE_LONG_FIELD_DESC = new org.apache.thrift.protocol.TField("valueLong", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField VALUE_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("valueList", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField EXT_FIELD_DESC = new org.apache.thrift.protocol.TField("ext", org.apache.thrift.protocol.TType.STRING, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ResLongListStrStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ResLongListStrTupleSchemeFactory());
  }

  public int res; // required
  public long valueLong; // required
  public List<String> valueList; // required
  public String ext; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    RES((short)1, "res"),
    VALUE_LONG((short)2, "valueLong"),
    VALUE_LIST((short)3, "valueList"),
    EXT((short)4, "ext");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // RES
          return RES;
        case 2: // VALUE_LONG
          return VALUE_LONG;
        case 3: // VALUE_LIST
          return VALUE_LIST;
        case 4: // EXT
          return EXT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __RES_ISSET_ID = 0;
  private static final int __VALUELONG_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.RES, new org.apache.thrift.meta_data.FieldMetaData("res", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.VALUE_LONG, new org.apache.thrift.meta_data.FieldMetaData("valueLong", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.VALUE_LIST, new org.apache.thrift.meta_data.FieldMetaData("valueList", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.EXT, new org.apache.thrift.meta_data.FieldMetaData("ext", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ResLongListStr.class, metaDataMap);
  }

  public ResLongListStr() {
  }

  public ResLongListStr(
    int res,
    long valueLong,
    List<String> valueList,
    String ext)
  {
    this();
    this.res = res;
    setResIsSet(true);
    this.valueLong = valueLong;
    setValueLongIsSet(true);
    this.valueList = valueList;
    this.ext = ext;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ResLongListStr(ResLongListStr other) {
    __isset_bitfield = other.__isset_bitfield;
    this.res = other.res;
    this.valueLong = other.valueLong;
    if (other.isSetValueList()) {
      List<String> __this__valueList = new ArrayList<String>(other.valueList);
      this.valueList = __this__valueList;
    }
    if (other.isSetExt()) {
      this.ext = other.ext;
    }
  }

  public ResLongListStr deepCopy() {
    return new ResLongListStr(this);
  }

  @Override
  public void clear() {
    setResIsSet(false);
    this.res = 0;
    setValueLongIsSet(false);
    this.valueLong = 0;
    this.valueList = null;
    this.ext = null;
  }

  public int getRes() {
    return this.res;
  }

  public ResLongListStr setRes(int res) {
    this.res = res;
    setResIsSet(true);
    return this;
  }

  public void unsetRes() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RES_ISSET_ID);
  }

  /** Returns true if field res is set (has been assigned a value) and false otherwise */
  public boolean isSetRes() {
    return EncodingUtils.testBit(__isset_bitfield, __RES_ISSET_ID);
  }

  public void setResIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RES_ISSET_ID, value);
  }

  public long getValueLong() {
    return this.valueLong;
  }

  public ResLongListStr setValueLong(long valueLong) {
    this.valueLong = valueLong;
    setValueLongIsSet(true);
    return this;
  }

  public void unsetValueLong() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __VALUELONG_ISSET_ID);
  }

  /** Returns true if field valueLong is set (has been assigned a value) and false otherwise */
  public boolean isSetValueLong() {
    return EncodingUtils.testBit(__isset_bitfield, __VALUELONG_ISSET_ID);
  }

  public void setValueLongIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __VALUELONG_ISSET_ID, value);
  }

  public int getValueListSize() {
    return (this.valueList == null) ? 0 : this.valueList.size();
  }

  public java.util.Iterator<String> getValueListIterator() {
    return (this.valueList == null) ? null : this.valueList.iterator();
  }

  public void addToValueList(String elem) {
    if (this.valueList == null) {
      this.valueList = new ArrayList<String>();
    }
    this.valueList.add(elem);
  }

  public List<String> getValueList() {
    return this.valueList;
  }

  public ResLongListStr setValueList(List<String> valueList) {
    this.valueList = valueList;
    return this;
  }

  public void unsetValueList() {
    this.valueList = null;
  }

  /** Returns true if field valueList is set (has been assigned a value) and false otherwise */
  public boolean isSetValueList() {
    return this.valueList != null;
  }

  public void setValueListIsSet(boolean value) {
    if (!value) {
      this.valueList = null;
    }
  }

  public String getExt() {
    return this.ext;
  }

  public ResLongListStr setExt(String ext) {
    this.ext = ext;
    return this;
  }

  public void unsetExt() {
    this.ext = null;
  }

  /** Returns true if field ext is set (has been assigned a value) and false otherwise */
  public boolean isSetExt() {
    return this.ext != null;
  }

  public void setExtIsSet(boolean value) {
    if (!value) {
      this.ext = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case RES:
      if (value == null) {
        unsetRes();
      } else {
        setRes((Integer)value);
      }
      break;

    case VALUE_LONG:
      if (value == null) {
        unsetValueLong();
      } else {
        setValueLong((Long)value);
      }
      break;

    case VALUE_LIST:
      if (value == null) {
        unsetValueList();
      } else {
        setValueList((List<String>)value);
      }
      break;

    case EXT:
      if (value == null) {
        unsetExt();
      } else {
        setExt((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case RES:
      return getRes();

    case VALUE_LONG:
      return getValueLong();

    case VALUE_LIST:
      return getValueList();

    case EXT:
      return getExt();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case RES:
      return isSetRes();
    case VALUE_LONG:
      return isSetValueLong();
    case VALUE_LIST:
      return isSetValueList();
    case EXT:
      return isSetExt();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ResLongListStr)
      return this.equals((ResLongListStr)that);
    return false;
  }

  public boolean equals(ResLongListStr that) {
    if (that == null)
      return false;

    boolean this_present_res = true;
    boolean that_present_res = true;
    if (this_present_res || that_present_res) {
      if (!(this_present_res && that_present_res))
        return false;
      if (this.res != that.res)
        return false;
    }

    boolean this_present_valueLong = true;
    boolean that_present_valueLong = true;
    if (this_present_valueLong || that_present_valueLong) {
      if (!(this_present_valueLong && that_present_valueLong))
        return false;
      if (this.valueLong != that.valueLong)
        return false;
    }

    boolean this_present_valueList = true && this.isSetValueList();
    boolean that_present_valueList = true && that.isSetValueList();
    if (this_present_valueList || that_present_valueList) {
      if (!(this_present_valueList && that_present_valueList))
        return false;
      if (!this.valueList.equals(that.valueList))
        return false;
    }

    boolean this_present_ext = true && this.isSetExt();
    boolean that_present_ext = true && that.isSetExt();
    if (this_present_ext || that_present_ext) {
      if (!(this_present_ext && that_present_ext))
        return false;
      if (!this.ext.equals(that.ext))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_res = true;
    list.add(present_res);
    if (present_res)
      list.add(res);

    boolean present_valueLong = true;
    list.add(present_valueLong);
    if (present_valueLong)
      list.add(valueLong);

    boolean present_valueList = true && (isSetValueList());
    list.add(present_valueList);
    if (present_valueList)
      list.add(valueList);

    boolean present_ext = true && (isSetExt());
    list.add(present_ext);
    if (present_ext)
      list.add(ext);

    return list.hashCode();
  }

  @Override
  public int compareTo(ResLongListStr other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRes()).compareTo(other.isSetRes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.res, other.res);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetValueLong()).compareTo(other.isSetValueLong());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValueLong()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.valueLong, other.valueLong);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetValueList()).compareTo(other.isSetValueList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValueList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.valueList, other.valueList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExt()).compareTo(other.isSetExt());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExt()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ext, other.ext);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ResLongListStr(");
    boolean first = true;

    sb.append("res:");
    sb.append(this.res);
    first = false;
    if (!first) sb.append(", ");
    sb.append("valueLong:");
    sb.append(this.valueLong);
    first = false;
    if (!first) sb.append(", ");
    sb.append("valueList:");
    if (this.valueList == null) {
      sb.append("null");
    } else {
      sb.append(this.valueList);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("ext:");
    if (this.ext == null) {
      sb.append("null");
    } else {
      sb.append(this.ext);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ResLongListStrStandardSchemeFactory implements SchemeFactory {
    public ResLongListStrStandardScheme getScheme() {
      return new ResLongListStrStandardScheme();
    }
  }

  private static class ResLongListStrStandardScheme extends StandardScheme<ResLongListStr> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ResLongListStr struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // RES
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.res = iprot.readI32();
              struct.setResIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VALUE_LONG
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.valueLong = iprot.readI64();
              struct.setValueLongIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // VALUE_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.valueList = new ArrayList<String>(_list8.size);
                String _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = iprot.readString();
                  struct.valueList.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setValueListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // EXT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ext = iprot.readString();
              struct.setExtIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ResLongListStr struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(RES_FIELD_DESC);
      oprot.writeI32(struct.res);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(VALUE_LONG_FIELD_DESC);
      oprot.writeI64(struct.valueLong);
      oprot.writeFieldEnd();
      if (struct.valueList != null) {
        oprot.writeFieldBegin(VALUE_LIST_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.valueList.size()));
          for (String _iter11 : struct.valueList)
          {
            oprot.writeString(_iter11);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.ext != null) {
        oprot.writeFieldBegin(EXT_FIELD_DESC);
        oprot.writeString(struct.ext);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ResLongListStrTupleSchemeFactory implements SchemeFactory {
    public ResLongListStrTupleScheme getScheme() {
      return new ResLongListStrTupleScheme();
    }
  }

  private static class ResLongListStrTupleScheme extends TupleScheme<ResLongListStr> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ResLongListStr struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetRes()) {
        optionals.set(0);
      }
      if (struct.isSetValueLong()) {
        optionals.set(1);
      }
      if (struct.isSetValueList()) {
        optionals.set(2);
      }
      if (struct.isSetExt()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetRes()) {
        oprot.writeI32(struct.res);
      }
      if (struct.isSetValueLong()) {
        oprot.writeI64(struct.valueLong);
      }
      if (struct.isSetValueList()) {
        {
          oprot.writeI32(struct.valueList.size());
          for (String _iter12 : struct.valueList)
          {
            oprot.writeString(_iter12);
          }
        }
      }
      if (struct.isSetExt()) {
        oprot.writeString(struct.ext);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ResLongListStr struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.res = iprot.readI32();
        struct.setResIsSet(true);
      }
      if (incoming.get(1)) {
        struct.valueLong = iprot.readI64();
        struct.setValueLongIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.valueList = new ArrayList<String>(_list13.size);
          String _elem14;
          for (int _i15 = 0; _i15 < _list13.size; ++_i15)
          {
            _elem14 = iprot.readString();
            struct.valueList.add(_elem14);
          }
        }
        struct.setValueListIsSet(true);
      }
      if (incoming.get(3)) {
        struct.ext = iprot.readString();
        struct.setExtIsSet(true);
      }
    }
  }

}

