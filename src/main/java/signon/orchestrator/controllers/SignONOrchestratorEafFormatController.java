// Copyright 2021-2023 FINCONS GROUP AG within the Horizon 2020
// European project SignON under grant agreement no. 101017255.

// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 

//     http://www.apache.org/licenses/LICENSE-2.0 

// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.

package signon.orchestrator.controllers;

import java.io.StringWriter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import signon.orchestrator.api.EafFormatApi;
import signon.orchestrator.model.EafFormatRequest;
import signon.orchestrator.model.EafFormatResponse;
import signon.orchestrator.model.Metadata;
import signon.orchestrator.eaf.api.ANNOTATIONDOCUMENT;
import signon.orchestrator.eaf.api.AlignableType;
import signon.orchestrator.eaf.api.AnnotationType;
import signon.orchestrator.eaf.api.ConstraintType;
import signon.orchestrator.eaf.api.HeadType;
import signon.orchestrator.eaf.api.LangType;
import signon.orchestrator.eaf.api.LingType;
import signon.orchestrator.eaf.api.PropType;
import signon.orchestrator.eaf.api.TierType;
import signon.orchestrator.eaf.api.TimeType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import signon.orchestrator.errors.exceptions.FileFormatNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;

@RestController
public class SignONOrchestratorEafFormatController implements EafFormatApi {

    private final static Logger logger = LoggerFactory.getLogger(SignONOrchestratorEafFormatController.class);

    private void createAndAddProp(String name, String value, ANNOTATIONDOCUMENT doc){
        PropType propX = new PropType();
        propX.setNAME(name);
        propX.setValue(value);
        doc.getHEADER().getPROPERTY().add(propX);
    }

    private final static String[] AUDIO_EXTENSIONS = {"wav", "WAV", "m4a", "M4A"};
    private final static String[] VIDEO_EXTENSIONS = {"mp4", "MP4", "mov", "MOV", "3gp", "3GP"};

    @Override
    public ResponseEntity<EafFormatResponse> getEafFormat(EafFormatRequest eafFormatRequest) {

        try {
            String filename = eafFormatRequest.getFileName().toString();
            if (filename.contains(".")){
                throw new FileFormatNotSupportedException("File name cannot contain the extension for the file given in input", eafFormatRequest);
            }
            boolean found = false;
            ANNOTATIONDOCUMENT annotationDocument = prepareDefaultAD();
            HeadType.MEDIADESCRIPTOR mediaDescriptor = new HeadType.MEDIADESCRIPTOR();
            mediaDescriptor.setMEDIAURL("./" + eafFormatRequest.getFileName() + "." + eafFormatRequest.getFileFormat());
            String format = eafFormatRequest.getFileFormat().toString();
            for (String audioExtension : AUDIO_EXTENSIONS){
                if (format.equals(audioExtension)){
                    if (format.equals("m4a") || format.equals("M4A")){
                        mediaDescriptor.setMIMETYPE("audio/mp4");
                    } else {
                    mediaDescriptor.setMIMETYPE("audio/wav");
                    }
                    found = true;
                }
            }
            for (String videoExtension : VIDEO_EXTENSIONS){
                if (format.equals(videoExtension)){
                    if (format.equals("m4a") || format.equals("M4A")){
                        mediaDescriptor.setMIMETYPE("video/quicktime");
                    } else if (format.equals("3gp") || format.equals("3GP")) {
                        mediaDescriptor.setMIMETYPE("video/3gpp");
                    } else {
                        mediaDescriptor.setMIMETYPE("video/mp4");
                    }
                    found = true;
                }
            }
            if (!found){
                throw new FileFormatNotSupportedException("The file format provided is not supported "+ eafFormatRequest.getFileFormat(), eafFormatRequest);
            }
            mediaDescriptor.setRELATIVEMEDIAURL(mediaDescriptor.getMEDIAURL());
            annotationDocument.getHEADER().getMEDIADESCRIPTOR().add(mediaDescriptor);
            Metadata metadata = eafFormatRequest.getMetadata();

            createAndAddProp("INPUT_LANGUAGE", metadata.getSourceLanguage().toString(), annotationDocument);
            createAndAddProp("ANNOTATION_LANGUAGE", metadata.getAnnotationLanguage().toString(), annotationDocument);
            createAndAddProp("MESSAGE_TYPE", metadata.getMessageType().toString(), annotationDocument);
            createAndAddProp("LANGUAGE_TYPE", metadata.getLanguageType().toString(), annotationDocument);
            createAndAddProp("REGISTER", metadata.getRegister().toString(), annotationDocument);
            createAndAddProp("AGE", metadata.getAge().toString(), annotationDocument);
            createAndAddProp("GENDER", metadata.getGender().toString(), annotationDocument);
            createAndAddProp("HEARINGSTATUS", metadata.getHearingStatus().toString(), annotationDocument);
            createAndAddProp("FILETYPE", metadata.getFileType().toString(), annotationDocument);
            createAndAddProp("USERID", metadata.getUserID(), annotationDocument);

            String annotationLanguage = metadata.getAnnotationLanguage().toString();
            annotationDocument.getLANGUAGE().get(0).setLANGID(annotationLanguage);
            annotationDocument.getLANGUAGE().get(0).setLANGLABEL(annotationLanguage);
            annotationDocument.getTIER().get(0).setLANGREF(annotationDocument.getLANGUAGE().get(0));

            addAnnotationsToAD(eafFormatRequest, annotationDocument);

            String eafData = generateXml(annotationDocument);

            EafFormatResponse eafFormatResponse = new EafFormatResponse();
            eafFormatResponse.setEafData(eafData);
            return new ResponseEntity<EafFormatResponse>(eafFormatResponse, HttpStatus.CREATED);

        } catch (JAXBException | DatatypeConfigurationException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ANNOTATIONDOCUMENT prepareDefaultAD() throws DatatypeConfigurationException{

        String linguisticTypeId = "default-lt";

        ANNOTATIONDOCUMENT annotationDocument = new ANNOTATIONDOCUMENT();
        annotationDocument.setAUTHOR("SignON Horizon 2020 Project");
        annotationDocument.setDATE(getXMLGregorianCalendarCurrentDate());
        annotationDocument.setFORMAT("3.0");
        annotationDocument.setVERSION("3.0");

        HeadType header = new HeadType();
        header.setMEDIAFILE("");
        header.setTIMEUNITS("milliseconds");
        annotationDocument.setHEADER(header);

        TierType tier = new TierType();
        tier.setLINGUISTICTYPEREF(linguisticTypeId);
        tier.setTIERID("default");
        annotationDocument.getTIER().add(tier);

        LingType lingType = new LingType();
        lingType.setGRAPHICREFERENCES(false);
        lingType.setLINGUISTICTYPEID(linguisticTypeId);
        lingType.setTIMEALIGNABLE(true);
        annotationDocument.getLINGUISTICTYPE().add(lingType);

        LangType langType = new LangType();
        langType.setLANGDEF("http://cdb.iso.org/lg/CDB-00130975-001");
        annotationDocument.getLANGUAGE().add(langType);

        List<List<String>> constraints = List.of(
            List.of("Time_Subdivision", "Time subdivision of parent annotation's time interval, no time gaps allowed within this interval"),
            List.of("Symbolic_Subdivision", "Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered"),
            List.of("Symbolic_Association", "1-1 association with a parent annotation"),
            List.of("Included_In", "Time alignable annotations within the parent annotation's time interval, gaps are allowed")
        );
        for (List<String> constraint : constraints) {
            ConstraintType constraintType = new ConstraintType();
            constraintType.setSTEREOTYPE(constraint.get(0));
            constraintType.setDESCRIPTION(constraint.get(1));
            annotationDocument.getCONSTRAINT().add(constraintType);
        }

        return annotationDocument;
    }

    private void addAnnotationsToAD(EafFormatRequest eafFormatRequest, ANNOTATIONDOCUMENT annotationDocument){

        TimeType.TIMESLOT timeSlotStart = new TimeType.TIMESLOT();
        timeSlotStart.setTIMESLOTID("ts1");
        timeSlotStart.setTIMEVALUE(Long.valueOf(eafFormatRequest.getAnnotations().get(0).getTimeStartMs()));
        TimeType.TIMESLOT timeSlotStop = new TimeType.TIMESLOT();
        timeSlotStop.setTIMESLOTID("ts2");
        timeSlotStop.setTIMEVALUE(Long.valueOf(eafFormatRequest.getAnnotations().get(0).getTimeStopMs()));
        TimeType timeType = new TimeType();
        timeType.getTIMESLOT().add(timeSlotStart);
        timeType.getTIMESLOT().add(timeSlotStop);
        annotationDocument.setTIMEORDER(timeType);

        AlignableType alignableType = new AlignableType();
        alignableType.setANNOTATIONID("a1");
        alignableType.setTIMESLOTREF1(timeSlotStart);
        alignableType.setTIMESLOTREF2(timeSlotStop);
        alignableType.setANNOTATIONVALUE(eafFormatRequest.getAnnotations().get(0).getTranscription());
        AnnotationType annotationType = new AnnotationType();
        annotationType.setALIGNABLEANNOTATION(alignableType);
        annotationDocument.getTIER().get(0).getANNOTATION().add(annotationType);

    }

    private XMLGregorianCalendar getXMLGregorianCalendarCurrentDate() throws DatatypeConfigurationException{

        Date date = new Date(System.currentTimeMillis());
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar gregorianCalendarDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return gregorianCalendarDate;
    }

    private String generateXml(ANNOTATIONDOCUMENT annotationDocument) throws JAXBException{
        JAXBContext jaxbContext = JAXBContext.newInstance(ANNOTATIONDOCUMENT.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://www.mpi.nl/tools/elan/EAFv3.0.xsd");
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(annotationDocument, sw);
        String xmlContent = sw.toString();
        return xmlContent;
    }
}