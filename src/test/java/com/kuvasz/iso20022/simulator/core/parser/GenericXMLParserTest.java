package com.kuvasz.iso20022.simulator.core.parser;

import com.kuvasz.iso20022.simulator.exception.ParsingException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenericXMLParserTest {

    private GenericXMLParser parser;

    @BeforeEach
    void setUp() {
        parser = new GenericXMLParser();
    }

    @Test
    void testParseValidPain001Message() throws ParsingException {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
                <CstmrCdtTrfInitn>
                    <GrpHdr>
                        <MsgId>TEST-MSG-001</MsgId>
                        <CreDtTm>2023-12-20T10:30:00Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <InitgPty>
                            <Nm>Test Company</Nm>
                        </InitgPty>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>PMT-001</PmtInfId>
                        <PmtMtd>TRF</PmtMtd>
                        <CdtTrfTxInf>
                            <PmtId>
                                <InstrId>INSTR-001</InstrId>
                            </PmtId>
                            <Amt>
                                <InstdAmt Ccy="EUR">1000.00</InstdAmt>
                            </Amt>
                        </CdtTrfTxInf>
                    </PmtInf>
                </CstmrCdtTrfInitn>
            </Document>
            """;

        MessageContext result = parser.parse(xmlContent);

        assertNotNull(result);
        assertEquals("TEST-MSG-001", result.getMessageId());
        assertEquals("pain.001.001.03", result.getMessageType());
        assertEquals("CstmrCdtTrfInitn", result.getMessageName());
        assertNotNull(result.getParsedDocument());
        assertTrue(result.getNamespaces().containsKey(""));
        assertEquals("urn:iso:std:iso:20022:tech:xsd:pain.001.001.03", result.getNamespaces().get(""));
    }

    @Test
    void testParseValidPacs008Message() throws ParsingException {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>PACS-MSG-001</MsgId>
                        <CreDtTm>2023-12-20T10:30:00Z</CreDtTm>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext result = parser.parse(xmlContent);

        assertNotNull(result);
        assertEquals("PACS-MSG-001", result.getMessageId());
        assertEquals("pacs.008.001.02", result.getMessageType());
        assertEquals("FIToFICstmrCdtTrf", result.getMessageName());
    }

    @Test
    void testParseInvalidXML() {
        String invalidXml = "<invalid><unclosed>";

        assertThrows(ParsingException.class, () -> parser.parse(invalidXml));
    }

    @Test
    void testParseEmptyContent() {
        assertThrows(ParsingException.class, () -> parser.parse(""));
        assertThrows(ParsingException.class, () -> parser.parse(null));
    }

    @Test
    void testParseXMLWithoutNamespace() throws ParsingException {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <CstmrCdtTrfInitn>
                    <GrpHdr>
                        <MsgId>NO-NS-MSG-001</MsgId>
                        <CreDtTm>2023-12-20T10:30:00Z</CreDtTm>
                    </GrpHdr>
                </CstmrCdtTrfInitn>
            </Document>
            """;

        MessageContext result = parser.parse(xmlContent);

        assertNotNull(result);
        assertEquals("NO-NS-MSG-001", result.getMessageId());
        assertEquals("unknown", result.getMessageType()); // Sin namespace no puede determinar el tipo
        assertEquals("CstmrCdtTrfInitn", result.getMessageName());
    }

    @Test
    void testCanHandle() {
        assertTrue(parser.canHandle("pain.001.001.03"));
        assertTrue(parser.canHandle("pacs.008.001.02"));
        assertTrue(parser.canHandle("camt.053.001.02"));
        assertTrue(parser.canHandle("any-message-type")); // GenericXMLParser maneja cualquier tipo
    }

    @Test
    void testGetParserType() {
        assertEquals("GENERIC_XML", parser.getParserType());
    }
}
