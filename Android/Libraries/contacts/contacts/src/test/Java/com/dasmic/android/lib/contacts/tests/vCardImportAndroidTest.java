package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 12/25/2015.
 */
public class vCardImportAndroidTest {
    private static final String CrLf ="\r\n";

    private final String vCardAndroid=
    "BEGIN:VCARD"+ CrLf +
    "VERSION:2.1"+ CrLf +
            "N:Belwal;Aditya;VCF;;"+ CrLf +
            "FN:Aditya Belwal"+ CrLf +
            "URL:http://www.google.com/profiles/109821110491634819626"+ CrLf +
            "PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE"+ CrLf +
            "CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/"+ CrLf +
            "2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC"+ CrLf +
            "goKCgoKCgoKCgoKCgr/wAARCABgAGADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAA"+ CrLf +
            "ECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaE"+ CrLf +
            "II0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZn"+ CrLf +
            "aGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJy"+ CrLf +
            "tLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAw"+ CrLf +
            "QFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobH"+ CrLf +
            "BCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hp"+ CrLf +
            "anN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0"+ CrLf +
            "tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9/KTcvrQfumvJf2lfi/"+ CrLf +
            "rHwz0iG30R1WWVfNmkMe/5N+NtRKfKK56m1xA6FxcLs9Q9eI/EH9qC2+H2p3duL63vWtpPs7w"+ CrLf +
            "I/wB99n3/APcr5z8T/tI+MtRnuCdduIoHk3+THO+xE/uV5X4w8e6hqE89xc3zsX9q8fEY2f2T"+ CrLf +
            "SEeY+hPHP7Y9teaAbfTrdotUlne4ur3f9z+4if8AAK4HQP2yvEvhPVZJ7a8SXLo0Hn/cR9//A"+ CrLf +
            "NhXg+oalq9yhkijdwP/AK9c9qsOsBHvBDKdnvXBKrJ1Oc1/en3Z8Hf2gv2jtS+D9x4j8DaPpO"+ CrLf +
            "uQ6Zu8u0+3Q+bs8z596ff/AIv79df+zj+3Lpvxd1aw8I/EPwk3h6/1iHzdDuftG621H+Lahbl"+ CrLf +
            "H/uIc7+1fmjovxd8e+Bp7248K+IrrT2v7F7K+8h/9dC/30f8A2PkrvPCvxxg+IGhab8Nb/Sor"+ CrLf +
            "K6in0pLHUbR9j+dDN87u/wDAnkv/AAfc2VSxeL/xi5IH61/6wN/1zrRh/wBW31rnvBWnaho3h"+ CrLf +
            "Wx0fVNZ+33Ftaokl7/z2/266FOr/QV6GU/BI5ySiiivYGRvIEg318p/tu61pq62txps8rXCIi"+ CrLf +
            "zh3+TZs+R0r6qmt47i2aCdAyuu16+bP209AsrWDS9I04t/pU7u6P3/AIK4cf8AwWOHxnymfCv"+ CrLf +
            "iXxTE9zZ6d8p/jrM0/wCFeu3erC3vLaVQJBvDpX06nhbTvD3h6C0WzRdkCVg6gsEUhcbK+Jr4"+ CrLf +
            "+dj6LC4CnI47Rvh14c8PaSqNZo0v8ckifWsXxb4X0aSByIIuY+f3dd3qUkZtz5ZZ3riPEgupJ"+ CrLf +
            "CJI1SvJlOpOoe0qVP2Z4L8Y/hfaQWE2q6enlbP+edeffCXWNG8M+MI7rxLodvqFv86Pa3bvsd"+ CrLf +
            "H+/s/29le9+LbJtQ06fTsqEfGa+evH3hrUvB/iA2+o27hGffBJ0R0r18txHP7k5nz2aYXlnzQ"+ CrLf +
            "P2b+GHxT8BePfBegXugXKpb69pTTadAPvukKojp/vpuSvQbVUEQMQ+XZ8tfnN+yX8XfCtz8Jv"+ CrLf +
            "BviTRvEF1ceIvCviNLSbR5J96Q2D7/t14iff2fZvv7/kTZ8n36/RDRZ7WXRrSS2lDxSQJ5b/A"+ CrLf +
            "N5dvWvocmlN160DxqsNIl5Li3LtGJF3J1prXdshx5q024e3G3zEqFP7KdMx5P8A33X0RBbcHZ"+ CrLf +
            "ivHP2jvBM3inxBoMkJzuuvs/mb/ub/ALleySEAZNc34m8KTa9PFJc6lKsdvOLiONPu70+5XHj"+ CrLf +
            "KNStQcIFUtKh4v8ZT4S8C3P2LV9ZiWRIP3af7HNeM6l8UvBd9cva2WrRPtr0r9tHwtp2qeJrU"+ CrLf +
            "2dsrXV5Ggmkk+5D1r5N17w3qnh7xRc2mUZYX/dzonyPXwtXC0/gPco42vBRPVdT8b6bBp8lz5"+ CrLf +
            "uVSOvGviJ+0VdQTPb6NorN/fkf7lel+HvAf/CSeDhqL70dJP3iV4l438FpHq9xHd2fmxDekcf"+ CrLf +
            "3NlcVKFD2/JM769XFew5oGdpnxT8Y6/qayNqthbo8n+pkp/wC0bbDWfhvbauI/9IsZ/n2f3Hq"+ CrLf +
            "r4f8AhtKNKOnvbJv8/wA37Ts2P/uV2HiDwcJ/hFqmk6hJ53+iu8byf7FVVqwo14chzKjXnQnz"+ CrLf +
            "nM/skah8efENld/BnwF4+s9G0/xEj+ZBqr/urz++n3Hf/gCffr9PPhufjX4LvPC+heP/AB1Ze"+ CrLf +
            "Iv7SSW01H7Doy2osHW3MqOg+9s+TyTv/jkTp9yvyj8Kapo82haTd2pdXhj8q68v+5X6efsJ6j"+ CrLf +
            "4h8W/CfTvGnia5vLpts1lpmo34dZbyxTydjlH/ALj+bDv/AI0hD/x19dktWc6kzxsTS9lCJ9A"+ CrLf +
            "VDNbxSR/vI91TUV9GcgVHNGHiapKbL/qz9R/WgDyL42eG7O/gi1ifa/7nZvr501/4f6ff6q8s"+ CrLf +
            "lpsiST95X098ViJvDksEUXzW8+8jZ9xHzXzl8RNcXTbOb7O3zV+b4yHJXnA+oy9U50PfJRa2W"+ CrLf +
            "jeF1j+WKGaTehryf4l6fpVncreXBSaKf5JHpvxM+N2sW8EenXOnJBa2qIsEnmf6yvJZfij4i8"+ CrLf +
            "Q3V7pF5qEDWtyieWn8aV50sPOfvo9L6xQ+A626060sEMllJvSsHxN4okPhu/tNnyeQ/wC7rOt"+ CrLf +
            "fEcisbdzhf+elYHjvUxcafcWMEu3fG/EdRClaZlXlT9mc/wCCZo/CPhyPxlJosGpRefsj06dN"+ CrLf +
            "6XL7/kh2f7dfrF+xH8Y9f+N/wIsfG3i3R7Gwu5mzBp2nRFYre3U7IkXPb909fhZF8XvEWleKb"+ CrLf +
            "G3huJZYNMvvNgtf4Hfn79fsx/wTX/aH8CfGr4W3n2XT7fTPEd1qM2o6npcEv7kea/8Ay7pn5E"+ CrLf +
            "RNibP+B9XavvMmo+xqTPksViIVpwifUVFFFfQHOFFFVr2/ttPtXvb24WKJE3O7/wAFAGL4w0F"+ CrLf +
            "9Y0+dbaGNnnhMT7/7vVf/AB+viX4+az/wj9jcXEh+eKTiOtD9pX/got42sLzUdB+H2oW9na+a"+ CrLf +
            "8cF2kf77bXhHiD9oGw+L/gGW8125/wBPtpE+2/7f+3XxWcewxFbnpHfgq/sfdOD+InxQttYuS"+ CrLf +
            "klm93K/+sk2fIled6t4o1QP5mm6Ekqf3I/k2f8AA67fxhdCTSDP4eTZv/8Ar15qbTXLycnWb5"+ CrLf +
            "AiP9yN64FKEIHoU/Z+zLvhfx1qkd+NNv4niWXf/rP79SePPH62Gi3fn3Kuxj2JXLeP/Ftvodu"+ CrLf +
            "tyD8ydZK8f8b/ABhOpSm2t7ncvHyfnU0aVStPm5Dnr4r2NPkMq612f/hI5lst7M8719f/ALFX"+ CrLf +
            "7QfiH4X+INM1zRJvLubF0ben3JP76V8keBfCtxqGonXNYudq/f8AIj/9nr1LwX4mg0fUHFnEs"+ CrLf +
            "I3p9yvpKMvZo+c5/wB4fv78G/id4c+Mfw707x/4duEaK7hHnQpJu8mbnfE3upOK60YUcdK+BP"+ CrLf +
            "8AglP+2d8INL+Hw+BXjPXxp2r3OsTXFldXb7YJvN2Yh3/wP8v6197JKXXFe9CfPT5jphNSPJP"+ CrLf +
            "2hvj7rHwjvI7DTrGL54PNE86/Wvk745ftg+PvGEEtlqGvMkHeCD5Er6u/a8+GbfEr4P3s2ixe"+ CrLf +
            "ZqmlI9zaxx43SqPvp+X/AI8K/LL4jeJ7m0u5re4DI2/rXh46dT2/x+4O5x/xW8dNJqd19oucf"+ CrLf +
            "PXnGnfEK503X4ojebYbmTyp/wDceq/xT15pLl7jGz/Jry/UNXuLicyQHp/HXmrkmZKqfRevfE"+ CrLf +
            "G48P2c2j3lxtaP/O+vL9Y+LMVjqD3In+cf9NKu634mHiXwRYXF7cQJcvAiPvk/jrzHV/Ct9fw"+ CrLf +
            "zSie3THR3kriowoTPSryr0aZS+KPxgn1xXstPuN6VQ8CeHjqFk/iK6g890/5Z4+RKsWHw/wBC"+ CrLf +
            "My3Wp6yt60ezzP7lXdS8fWOiRm30/YsUf+rjT7letRpYeHwHjVas60zbfxDFaaWtsDt/v8Vm6"+ CrLf +
            "b4wlubg21rLhx/44lcPrXjp9QZdO0443/wb/wCOrXhfT/ENhPLeuHdnj+St4cn85zuVz3TwL8"+ CrLf +
            "UF8M6hHM9zdSp8nlwQSbN71+mH7Cn/AAU1udOsbD4e/HWKeLTFg8q01meTzXtz/Aj/AMbrx1r"+ CrLf +
            "8iPA2vWmnawuo6zbTpvf/AFn33/74r2/w18QdM04RyadMkq/7CPXRSr+xJjVqUz//2Q=="+ CrLf +
            CrLf +
            "EMAIL;PREF:abelwal@gmail.com"+ CrLf +
            "END:VCARD";

    @Test
    public void testSet() throws Exception {
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCardAndroid);
        assertTrue("testSetFromVCardOutlookString",
                dct.getVCardName().equals("Aditya VCF Belwal"));
        assertTrue("testSetFromVCardOutlookString",
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:abelwal@gmail.com\r\n")
        );
    }
}
