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

package com.alibaba.polardbx.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.polardbx.executor.sync.CreateJavaFunctionSyncAction;
import com.alibaba.polardbx.executor.sync.DropJavaFunctionSyncAction;
import com.alibaba.polardbx.server.response.ShowNodeSyncAction;
import com.alibaba.polardbx.server.response.ShowSQLSlowSyncAction;
import org.junit.Assert;
import org.junit.Test;

public class SyncActionTest {

    @Test
    public void testShowSqlJson() {
        ShowSQLSlowSyncAction showNodeSyncAction = new ShowSQLSlowSyncAction();
        String data = JSON.toJSONString(showNodeSyncAction, SerializerFeature.WriteClassName);
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setAutoTypeSupport(true);
        Object obj = JSON.parse(data);
        Assert.assertEquals(showNodeSyncAction.getClass(), obj.getClass());
    }

    @Test
    public void testShowNodeJson() {
        ShowNodeSyncAction showNodeSyncAction = new ShowNodeSyncAction("Test");
        String data = JSON.toJSONString(showNodeSyncAction, SerializerFeature.WriteClassName);
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setAutoTypeSupport(true);
        Object obj = JSON.parse(data);
        Assert.assertEquals(showNodeSyncAction.getClass(), obj.getClass());
    }

    @Test
    public void testCreateFunctionJson() {
        CreateJavaFunctionSyncAction createJavaFunctionSyncAction = new CreateJavaFunctionSyncAction("Test");
        String data = JSON.toJSONString(createJavaFunctionSyncAction, SerializerFeature.WriteClassName);
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setAutoTypeSupport(true);
        Object obj = JSON.parse(data);
        Assert.assertEquals(createJavaFunctionSyncAction.getClass(), obj.getClass());
    }

    @Test
    public void testDropFunctionJson() {
        DropJavaFunctionSyncAction dropJavaFunctionSyncAction = new DropJavaFunctionSyncAction("Test");
        String data = JSON.toJSONString(dropJavaFunctionSyncAction, SerializerFeature.WriteClassName);
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setAutoTypeSupport(true);
        Object obj = JSON.parse(data);
        Assert.assertEquals(dropJavaFunctionSyncAction.getClass(), obj.getClass());
    }
}
