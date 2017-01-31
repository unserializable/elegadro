package org.elegadro.parser.util.rt;

import _2010._02.tyviseadus_1_10.*;

import javax.xml.bind.JAXBElement;

/**
 * @author Taimo Peelo
 */
public final class LawParseUtil {
    private LawParseUtil() {}

    public static final String textTypeToString(_2010._02.tyviseadus_1_10.TekstType tekstType) {
        StringBuilder sb = new StringBuilder();
        for (Object o: tekstType.getTabelOrTavatekstOrLegaaldefinitsioon()) {
            if (o instanceof TypeID) {
                TypeID tid = (TypeID) o;
                sb.append(typeID2String(tid));
            } else if (o instanceof TavatekstID) {
                sb.append(tavatekstIDToString((TavatekstID) o));
            } else if (o instanceof StringID) {
                return stringIDToString((StringID)o);
            }
            else {
                throw new UnsupportedOperationException(" unhandled tekstType " + o.getClass());
            }
        }

        return sb.toString();
    }

    // Sample content:
    //    "Veaparandus. Parandatud ilmne ebatäpsus lõike numbris 5² Riigi Teataja seaduse § 10 lõike 4 alusel."
    // Since we are not handling the timelines atm (also see comments to #muutmismargeTypeToString(...)
    // these are ignored as they provide no additional information
    public static final String veaparandusTypeToString(_2010._02.tyviseadus_1_10.VeaparandusType veaparandusType) {
        return "";
    }

    // Since the law nodes ARE NOT versioned currently, this change record will have no textual representation
    // Even if we took the timelines into account now, the change record(s) would be EITHER:
    //    a) additional node attributes
    //    b) additional path relation in graph -- dated suitably.
    // Ergo: textual represenation is empty in the context here.
    public static final String muutmismargeTypeToString(_2010._02.tyviseadus_1_10.MuutmismargeType muutmismargeType) {
        return "";
    }

    // THe attached URIs do not sometimes make apparent sense even in Riigi Teataja.
    // Ignore the URI for now, use the display text only.
    public static final String tekstTypeViideToString(_2010._02.tyviseadus_1_10.TekstType.Viide tekstViide) {
        StringID kuvatavTekst = tekstViide.getKuvatavTekst();
        return (kuvatavTekst != null) ? stringIDToString(kuvatavTekst) : "";
    }

    public static final String stringIDToString(_2010._02.tyviseadus_1_10.StringID stringID) {
        /* This is really fucking glitchy. This stringID brings shit like.
            <p align="center"><b>2. TEOSE KASUTAMINE AUTORI NÕUSOLEKUTA JA TASU MAKSMISETA</b></p>
            <p align="center"><b>3. TEOSE KASUTAMINE AUTORI NÕUSOLEKUTA, KUID TASU MAKSMISEGA</b></p>
            <p align="center"><b>4. ORBTEOSE KASUTAMINE</b><br/>
                [<a href="./129102014002">RT I, 29.10.2014, 2</a> - jõust. 30.10.2014]</p>
            <p align="center"><b>2. AUTORILEPING</b></p>
        */
        return stringID.getValue();
    }

    public static final String tavatekstIDToString(_2010._02.tyviseadus_1_10.TavatekstID tavatekstID) {
        StringBuilder sb = new StringBuilder();

        for (Object o: tavatekstID.getContent()) {
            if (o instanceof TypeID) {
                TypeID tid = (TypeID) o;
                sb.append(typeID2String(tid));
            }
            else if (o instanceof JAXBElement) {
                Object value = ((JAXBElement) o).getValue();
                if (value instanceof TavatekstID) {
                    sb.append(tavatekstIDToString(((TavatekstID)value)));
                } else if (value instanceof Loend) {
                    sb.append("UNRESOLVED_LOEND: " + value);
                } else if (value instanceof StringID) {
                    sb.append(stringIDToString((StringID) value));
                }
            }
            else if (o instanceof String) {
                sb.append((String)o);
            }  else {
                throw new UnsupportedOperationException("Unknown TavaTekst content "  + o.getClass().getName());
            }
        }
        return sb.toString();
    }

    private static final String typeID2String(TypeID typeID) {
        if (typeID instanceof _2010._02.tyviseadus_1_10.TekstType.Viide) {
            TekstType.Viide tekstViide = (TekstType.Viide) typeID;
            return (tekstTypeViideToString(tekstViide));
        } else if (typeID instanceof _2010._02.tyviseadus_1_10.MuutmismargeType) {
            MuutmismargeType mm = (MuutmismargeType) typeID;
            return (muutmismargeTypeToString(mm));
        }
        else if (typeID instanceof _2010._02.tyviseadus_1_10.VeaparandusType) {
            VeaparandusType vp = (VeaparandusType) typeID;
            return (veaparandusTypeToString(vp));
        } else {
            return ("{UNRESOLVED: " + typeID.getClass() + "}");
        }
    }
}
