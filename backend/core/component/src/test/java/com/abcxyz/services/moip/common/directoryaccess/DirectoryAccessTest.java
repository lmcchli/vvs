package com.abcxyz.services.moip.common.directoryaccess;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.easymock.classextension.EasyMock;
import org.junit.Ignore;
import org.junit.Test;
import static org.easymock.classextension.EasyMock.*;

import com.abcxyz.messaging.common.mcd.MCDException;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;


public class DirectoryAccessTest {

    @Test
    public void testBasicLookup() {
        MCDProxyService mockProxy = EasyMock.createNiceMock(MCDProxyService.class);
        Profile testsub = createMoipTestSub("491721093078");
        Profile testcos = createMoipTestCos("testcos");
        try {
            expect(mockProxy.lookupProfile(eq("subscriber"), isA(URI.class), (String)anyObject())).andReturn(testsub);
            expect(mockProxy.lookupProfile(eq("classofservice"), isA(URI.class), (String)anyObject())).andReturn(testcos);
            replay(mockProxy);
            DirectoryAccess da = DirectoryAccess.getInstance();
            da.setMcdProxy(mockProxy);
            IDirectoryAccessSubscriber sub = da.lookupSubscriber("tel:" + "491721093078");
            assertNotNull(sub);
            
            String[] cosValues = sub.getStringAttributes("MOIPCosIdentity");
            assertTrue(cosValues.length == 1);
            assertTrue(cosValues[0].equals("cos:testcos"));
            int pinMinVals[] = sub.getIntegerAttributes("MOIPPinMinLen");
            assertTrue(pinMinVals[0] == 4);
            
            verify(mockProxy);
        } 
        catch (MCDException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testLookupReturnsEvenNonMoipAttributes() {
        MCDProxyService mockProxy = EasyMock.createNiceMock(MCDProxyService.class);
        Profile testsub = createMoipTestSubIncludingMoreAttributes("491721093078", "fmctestcos");
        Profile testcos = createMoipTestCos("testcos");
        Profile testfmccos = createFmcTestCos("fmctestcos");
        try {
            expect(mockProxy.lookupProfile(eq("subscriber"), isA(URI.class), (String)anyObject())).andReturn(testsub);
            expect(mockProxy.lookupProfile(eq("classofservice"), eq(new URI("cos:" + "testcos")), (String)anyObject())).andReturn(testcos);
            expect(mockProxy.lookupProfile(eq("classofservice"), eq(new URI("cos:" + "fmctestcos")), (String)anyObject())).andReturn(testfmccos);
            replay(mockProxy);
            DirectoryAccess da = DirectoryAccess.getInstance();
            da.setMcdProxy(mockProxy);
            IDirectoryAccessSubscriber sub = da.lookupSubscriber("tel:" + "491721093078");
            assertNotNull(sub);
            
            String[] cosValues = sub.getStringAttributes("MOIPCosIdentity");
            assertTrue(cosValues.length == 1);
            assertTrue(cosValues[0].equals("cos:testcos"));
            int pinMinVals[] = sub.getIntegerAttributes("MOIPPinMinLen");
            assertTrue(pinMinVals[0] == 4);
            
            String[] fmcCosAttribute = sub.getStringAttributes("FMCCosTestAttribute");
            assertTrue(fmcCosAttribute[0].equals("onecosttestattributevalue"));
            
            String[] fmcSubAndCosAttribute = sub.getStringAttributes("FMCTestAttribute");
            assertTrue(fmcSubAndCosAttribute[0].equals("onetestattribute"));
            
            verify(mockProxy);
        } 
        catch (MCDException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testSubHasFMCServiceOnly(){
        
    }
    
    private Profile createMoipTestSub(String aSubTel){
        Profile mySub = new ProfileContainer();
        mySub.addIdentity("muid:"+aSubTel);
        mySub.addIdentity("tel:" + "+" + aSubTel);
        mySub.addAttributeValue("MOIPPin", "1111");
        mySub.addAttributeValue("MOIPCn", "test sub");
        mySub.addAttributeValue("MOIPCosIdentity", "cos:testcos");
        
        return mySub;
    }
    
    
    private Profile createMoipTestCos(String aCosIdentity){
        Profile myCos = new ProfileContainer();
        myCos.addIdentity("muid:"+aCosIdentity);
        myCos.addIdentity("cos:" + "+" + aCosIdentity);
        myCos.addAttributeValue("MOIPPinMaxLen", "8");
        myCos.addAttributeValue("MOIPPinMinLen", "4");
        
        return myCos;
    }
    
    private Profile createMoipTestSubIncludingMoreAttributes(String aSubTel, String anFmcCosIdentity){
        Profile mySub = new ProfileContainer();
        mySub.addIdentity("muid:"+aSubTel);
        mySub.addIdentity("tel:" + "+" + aSubTel);
        mySub.addAttributeValue("MOIPPin", "1111");
        mySub.addAttributeValue("MOIPCn", "test sub");
        mySub.addAttributeValue("MOIPCosIdentity", "cos:testcos");
        mySub.addAttributeValue("FMCCosIdentity", "cos:" + anFmcCosIdentity);
        mySub.addAttributeValue("FMCTestAttribute", "onetestattribute");
        
        return mySub;
    }
    
    
    private Profile createFmcTestCos(String anFmcCosIdentity){
        Profile myFmcCos = new ProfileContainer();
        myFmcCos.addIdentity("muid:"+anFmcCosIdentity);
        myFmcCos.addIdentity("cos:" + "+" + anFmcCosIdentity);
        myFmcCos.addAttributeValue("FMCCosTestAttribute", "onecosttestattributevalue");
        myFmcCos.addAttributeValue("FMCTestAttribute", "onecostestvalue");
        return myFmcCos;
    }
}
