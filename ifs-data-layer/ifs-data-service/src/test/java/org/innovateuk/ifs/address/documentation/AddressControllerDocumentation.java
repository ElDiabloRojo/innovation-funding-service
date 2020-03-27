package org.innovateuk.ifs.address.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.address.controller.AddressController;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.transactional.AddressLookupService;
import org.innovateuk.ifs.address.transactional.AddressService;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.AddressDocs.addressResourceBuilder;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;

public class AddressControllerDocumentation extends BaseControllerMockMVCTest<AddressController> {

    @Mock
    private AddressLookupService addressLookupServiceMock;

    @Mock
    private AddressService addressServiceMock;

    @Override
    protected AddressController supplyControllerUnderTest() {
        return new AddressController();
    }

    @Test
    public void validate() throws Exception {
        String postCode = "BA12LN";

        when(addressLookupServiceMock.validatePostcode(postCode)).thenReturn(serviceSuccess(true));

        mockMvc.perform(get("/address/validate-postcode/?postcode=" +  postCode)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("address/{method-name}",
                        requestParameters(
                                parameterWithName("postcode").description("Postcode to validate")
                        )
                ));
    }


    @Test
    public void lookup() throws Exception {
        int numberOfAddresses = 2;
        String postCode = "BS348XU";
        List<AddressResource> addressResources = addressResourceBuilder.build(numberOfAddresses);

        when(addressLookupServiceMock.doLookup(postCode)).thenReturn(serviceSuccess(addressResources));

        mockMvc.perform(get("/address/do-lookup/?lookup=" + postCode)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("address/{method-name}",
                        requestParameters(
                                parameterWithName("lookup").description("Postcode to look up")
                        ),
                        responseFields(
                                fieldWithPath("[]id").description("Address Id"),
                                fieldWithPath("[]addressLine1").description("Address line1"),
                                fieldWithPath("[]addressLine2").description("Address line2"),
                                fieldWithPath("[]addressLine3").description("Address Line3"),
                                fieldWithPath("[]town").description("Town"),
                                fieldWithPath("[]county").description("County"),
                                fieldWithPath("[]postcode").description("Postcode"),
                                fieldWithPath("[]country").description("Country")

                        )
                ));
    }
    @Test
    public void findOne() throws Exception {
        long id = 1;
        AddressResource addressResource = addressResourceBuilder.build();
        when(addressServiceMock.getById(id)).thenReturn(serviceSuccess(addressResource));

        mockMvc.perform(get("/address/{id}", id)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("address/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of Address to find")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Address Id"),
                                fieldWithPath("addressLine1").description("Address line1"),
                                fieldWithPath("addressLine2").description("Address line2"),
                                fieldWithPath("addressLine3").description("Address Line3"),
                                fieldWithPath("town").description("Town"),
                                fieldWithPath("county").description("County"),
                                fieldWithPath("postcode").description("Postcode"),
                                fieldWithPath("country").description("Country")
                                )
                ));
    }

}
