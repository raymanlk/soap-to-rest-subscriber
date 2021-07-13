package com.soap.rest.external.util;

public class ReplacementConstants {
    public static final String generateFilePom = "<schemaLanguage>WSDL</schemaLanguage>\n" +
            "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
            "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
            "\t\t\t\t\t<schemaIncludes>\n" +
            "\t\t\t\t\t\t<include>*.xsd</include>\n" +
            "\t\t\t\t\t\t<include>*.xml</include>\n" +
            "\t\t\t\t\t</schemaIncludes>";

    public static final String generateZipPom = "<schemaLanguage>WSDL</schemaLanguage>\n" +
            "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
            "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
            "\t\t\t\t\t<schemaIncludes>\n" +
            "\t\t\t\t\t\t<include>*.xml</include>\n" +
            "\t\t\t\t\t</schemaIncludes>";

    public static final String generateUrlPomStart = "" +
            "\t\t\t\t\t<schemaLanguage>WSDL</schemaLanguage>\n" +
            "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
            "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
            "\t\t\t\t\t<schemas>\n" +
            "\t\t\t\t\t\t<schema>\n" +
            "\t\t\t\t\t\t\t<url>";

    public static final String generateUrlPomEnd = "</url>\n" +
            "\t\t\t\t\t\t</schema>\n" +
            "\t\t\t\t\t</schemas>";

    private ReplacementConstants() {}
}
