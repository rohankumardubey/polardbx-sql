/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.optimizer.core.function.calc;

import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.exception.code.ErrorCode;
import com.alibaba.polardbx.common.utils.logger.Logger;
import com.alibaba.polardbx.common.utils.logger.LoggerFactory;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.CharType;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.datatype.DecimalType;
import com.alibaba.polardbx.optimizer.core.datatype.VarcharType;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class UserDefinedJavaFunction extends AbstractScalarFunction {
    protected static final Logger logger = LoggerFactory.getLogger(UserDefinedJavaFunction.class);
    protected List<DataType> userInputType;
    protected DataType userResultType;
    private Class curClazz;

    protected UserDefinedJavaFunction(List<DataType> operandTypes, DataType resultType) {
        super(operandTypes, resultType);
    }

    @Override
    public Object compute(Object[] args, ExecutionContext ec) {
        if (args.length != userInputType.size()) {
            throw new TddlRuntimeException(ErrorCode.ERR_EXECUTOR, "Parameters do not match input types");
        }
        //对入参进行处理
        Class[] inputClazz = new Class[userInputType.size()];
        for (int i = 0; i < args.length; i++) {
            DataType type = userInputType.get(i);

            if (type instanceof VarcharType || type instanceof CharType) {
                args[i] = DataTypes.StringType.convertFrom(args[i]);
                inputClazz[i] = DataTypes.StringType.getDataClass();
                continue;
            }

            if (type instanceof DecimalType) {
                args[i] = DataTypes.DecimalType.convertFrom(args[i]).toBigDecimal();
                inputClazz[i] = BigDecimal.class;
            }

            args[i] = type.convertFrom(args[i]);
            inputClazz[i] = type.getDataClass();
        }

        Method userMethod;
        Object result;
        try {
            if (userInputType.isEmpty()) {
                userMethod = curClazz.getMethod("compute");
            } else {
                userMethod = curClazz.getMethod("compute", inputClazz);
            }
            result =
                userMethod.invoke(curClazz.getDeclaredConstructor(List.class, DataType.class).newInstance(null, null),
                    args);
        } catch (Exception e) {
            throw new TddlRuntimeException(ErrorCode.ERR_EXECUTOR, "Cannot find such method according to input types");
        }

        return resultType.convertFrom(result);
    }

    public void setUserInputType(List<DataType> userInputType) {
        this.userInputType = userInputType;
    }

    public void setUserResultType(DataType userResultType) {
        this.userResultType = userResultType;
    }

    public void setClazz(Class curClazz) {
        this.curClazz = curClazz;
    }
}
