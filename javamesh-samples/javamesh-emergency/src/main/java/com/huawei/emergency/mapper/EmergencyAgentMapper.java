package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyAgent;
import com.huawei.emergency.entity.EmergencyAgentExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmergencyAgentMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    long countByExample(EmergencyAgentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int deleteByExample(EmergencyAgentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int insert(EmergencyAgent record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int insertSelective(EmergencyAgent record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    List<EmergencyAgent> selectByExample(EmergencyAgentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    EmergencyAgent selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") EmergencyAgent record, @Param("example") EmergencyAgentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") EmergencyAgent record, @Param("example") EmergencyAgentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(EmergencyAgent record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table emergency_agent
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(EmergencyAgent record);
}