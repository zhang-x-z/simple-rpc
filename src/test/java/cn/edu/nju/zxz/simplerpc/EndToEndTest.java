package cn.edu.nju.zxz.simplerpc;

import cn.edu.nju.zxz.simplerpc.annotation.RPCInterface;
import cn.edu.nju.zxz.simplerpc.annotation.RPCService;
import cn.edu.nju.zxz.simplerpc.exceptions.SimpleRPCInvocationException;
import cn.edu.nju.zxz.simplerpc.network.RPCServer;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.NettyRPCServer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EndToEndTest {
    public static class TestClass {
        private String testString;
        private Integer testInt;
        private List<String> testList;

        public String getTestString() {
            return testString;
        }

        public void setTestString(String testString) {
            this.testString = testString;
        }

        public Integer getTestInt() {
            return testInt;
        }

        public void setTestInt(Integer testInt) {
            this.testInt = testInt;
        }

        public List<String> getTestList() {
            return testList;
        }

        public void setTestList(List<String> testList) {
            this.testList = testList;
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "testString='" + testString + '\'' +
                    ", testInt=" + testInt +
                    ", testList=" + testList +
                    '}';
        }
    }

    public interface TestInterface {
        void testVoid();
        TestClass testCustomClass(TestClass argA, String argB, Integer argC, List<String> argD);
        void testException();
    }

    @RPCService(rpcInterface = TestInterface.class, token = "1243345")
    public static class TestImpl implements TestInterface {
        private String test = "";

        @Override
        public void testVoid() {
            System.out.println("testVoid");
        }

        @Override
        public TestClass testCustomClass(TestClass argA, String argB, Integer argC, List<String> argD) {
            System.out.println("testCustomClass");
            System.out.println(argA);
            System.out.println(argB);
            System.out.println(argC);
            System.out.println(argD);
            TestClass testClass = new TestClass();
            testClass.setTestInt(12);
            testClass.setTestString("test");
            List<String> res = new ArrayList<>();
            res.add("test1");
            res.add("test2");
            res.add("test3");
            testClass.setTestList(res);
            return testClass;
        }

        @Override
        public void testException() {
            throw new RuntimeException("test exception.");
        }
    }

    @Test
    public void testServer() throws Exception {
        SimpleRPCServiceRegister.registerRPCService(new TestImpl());
        RPCServer rpcServer = new NettyRPCServer();
        rpcServer.start(18080);
    }

    public static class TestClient {
        @RPCInterface(serverAddress = "localhost:18080", token = "1243345")
        private TestInterface testInterface;

        public void testInvokeVoid() {
            testInterface.testVoid();
        }

        public TestClass testInvokeCustomClass(TestClass argA, String argB, Integer argC, List<String> argD) {
            return testInterface.testCustomClass(argA, argB, argC, argD);
        }

        public void testInvokeException() {
            testInterface.testException();
        }
    }

    @Test
    public void testClient() {
        TestClient testClient = new TestClient();
        SimpleRPCClientRegister.registerRPCClient(testClient);
        try {
            testClient.testInvokeException();
        } catch (SimpleRPCInvocationException e) {
            e.printStackTrace();
        }
        testClient.testInvokeVoid();
        TestClass argA = new TestClass();
        argA.setTestString("A");
        argA.setTestInt(100);
        List<String> argList = new ArrayList<>();
        argList.add("testA1");
        argList.add("testA2");
        argList.add("testA3");
        argA.setTestList(argList);
        TestClass res = testClient.testInvokeCustomClass(argA, "argB", 34, argList);
        System.out.println(res);
    }

    public static class TestWrongTokenClient {
        @RPCInterface(serverAddress = "localhost:18080", token = "124")
        private TestInterface testInterface;

        public void testInvokeVoid() {
            testInterface.testVoid();
        }
    }

    @Test
    public void testWrongToken() {
        TestWrongTokenClient client = new TestWrongTokenClient();
        SimpleRPCClientRegister.registerRPCClient(client);
        try {
            client.testInvokeVoid();
        } catch (SimpleRPCInvocationException e) {
            e.printStackTrace();
        }
    }
}
