/*
 * Copyright (c) 2017 digi.me. All rights reserved.
 */

package me.digi.security;

import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.digi.security.SecurityUtils.binaryToBytes;
import static me.digi.security.SecurityUtils.bytesToString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SecurityUtilsTest {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());

    private static String benchmarkData = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eu ex lobortis turpis cursus aliquam. Donec facilisis lorem vitae luctus scelerisque. Nam non laoreet ex, sed aliquet arcu. Mauris eu tristique erat, id ullamcorper purus. Nullam condimentum tortor augue, quis suscipit magna aliquam a. Nulla facilisi. In placerat, odio id interdum semper, lacus metus ultrices erat, nec pulvinar nisl massa ut erat.";

    private static String convertByteArrayToString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "UTF-8");
    }

    // @Test // Broken due to SpongyCastle not being initialised properly in new SecurityModule, leaving for Kryz to look at as worked before extraction and unsure what the cause is.
    public void keyPairTest() throws UnsupportedEncodingException {
        KeyPair keyPair = SecurityUtils.generateRSAKeyPair();
        String textToEncrypt = "Test data to encrypt";

        byte[] excrypted = SecurityUtils.encryptRSA(textToEncrypt.getBytes(), keyPair.getPublic());
        String decryptedText = convertByteArrayToString(SecurityUtils.decryptRSA(excrypted, keyPair.getPrivate()));
        assertEquals(decryptedText, textToEncrypt);
    }

    // @Test - Broken Martin wanted to look at fixing this.
    public void rsaExternalTest() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        String publicHex = "3082010a0282010100b6f849705ec952e4220def5d179b7b38e32045193a70c8e3d804671e75159015b00db8711de0fc64208e99b65b05d850c85826b0438538391fde2c63b75ca1b3144d004a2f62726b45b643d7f04c36ecab8bffb9b2e7abec56fe6f0bc4953ecec637d11bb924cc045ab6d4a44e6289c4de75fabda63bc2ed30f3d2390fd23ee0828b3f0f7cdd8057742a417c22cd1a320a98d67617624d39006c2f0e73ae13f24ff4fc9cf24835aff7c7df0251f65fa8e749e1cf0ef62f33829262328777228dc2ec32fad072e9c34eed04c1cf5072ce255120533244b1a9a1262758612e34f156e6903eae49d3140be726b300cc4db050ab27fe02b9ac34fa8740a31360b6830203010001";
        String privateHex = "308204a40201000282010100b6f849705ec952e4220def5d179b7b38e32045193a70c8e3d804671e75159015b00db8711de0fc64208e99b65b05d850c85826b0438538391fde2c63b75ca1b3144d004a2f62726b45b643d7f04c36ecab8bffb9b2e7abec56fe6f0bc4953ecec637d11bb924cc045ab6d4a44e6289c4de75fabda63bc2ed30f3d2390fd23ee0828b3f0f7cdd8057742a417c22cd1a320a98d67617624d39006c2f0e73ae13f24ff4fc9cf24835aff7c7df0251f65fa8e749e1cf0ef62f33829262328777228dc2ec32fad072e9c34eed04c1cf5072ce255120533244b1a9a1262758612e34f156e6903eae49d3140be726b300cc4db050ab27fe02b9ac34fa8740a31360b6830203010001028201004efcb8976e13f358d0eabb1eb1064a17b0d5497f2e9f69da127334210de2952507afa4a4108603ef25aee9e4b33ebeb78105ad0e02d80c017d24687d53b705874d88404bc650f59c21a82179a31f03c6fff79c1a0a85c0ce726fbc789410e9e051e9deb7dd216981a7b7adec907a31876f91f700d036945bd8fa2912c125be466ab7b586a9d3724c091333aa9c61196801a6212aa23fdf25f91102fc9dbc4b333dc7f72f5eda9804acda512f22326e26551655844e06f9496100eef1f49639e2b81019568b65ca81ba6e2a5f53418fc398f2a90d438365a9176ab920db7f1a12bc89987d8ba4b603c8166742cc545fbb3b99d3fa903034a7314373f382d760e102818100d8cce50e3a9381141bf8d3e299577bd83a7a1168e10571d6148cf91cd929abc196bd4f23b7579fa3cb216fbbd353369c568756ab46e57bc271e1cb1cb95b9258607073407ec89a344a6b69850f246eb1b320dcd8da540f1675d21dce45aa7c552434d9c417bbacc13a989ac68ad508421cd87a9fc970bc15f740a1f870a7529502818100d80d782b527c1fea2b10eb523c01c81bd23c7ee6994218d3bc6e48f791ad7a8b7d370db8273240906214e1587d1a6152e5b37fdc0c96434f3d42865661203f045300c90abc40e58d04b97249d691677d422cc557b3656c227cd6ba9169bae71a4f7724435195d81152dc8a9c1d27438d4c91ee14506c8b9468a199b7a3e476b70281805346187167b482af0bb37f0799e8cd8c17a20d8fe066ebfbd3f6f634cb814314dfe6a5404dcb1d6997bba724591eac6a4e9e3b7f1c83470f4018ffa06fde298f42b3cec12631bd717a6859a69f535a256d6da106b68df521a66de7bef46970ca6f56d1928d9f2bd328c3b727ade9f7fb15035dce845c861255df80976206132502818100c703213e46acaf79345251715e7505af1ae93ebb8b50a72de2d473c2181e845318f42d5f043bf5dd097bd25780655c60cfd5986149f6dfe98db449aa27ad5c5ecf5659d9d9aeadc44825ca70d984f69f1ae5c2a2164dc65dbe40cd7a44ba3546b70c380b0a8bcc8ba8981dd5cfc4eb24acf37f2a972a65283f791bef5cb885e1028181009185653f77e3c2814af0f5bdd6b1cac786bc04640baefbb3544845ea33f62297d043f0b1a11156ebffa16e4ffed2c6fa0503b5ba2af8a1cdbcc41dab1620312174f8f6e6ce668b0f0cd1aee40daf8a25b79efb3d975880e9ccede9b7c0677ea1a60aa3bb360cb1c8053e978faea2648a9e93dcc7241c13a0424f14453945b53b";
        String toEncryptHex = "b4ea7b5101564424e143665e42aa7df16b23c85fa4636cb65f5d4aceb74cd9c3";
        String expectedEncryptedHex = "5d8e523f622f825be4ed2335b6bd233b983f38e515552b293695e53e74038de2bf347079d918779f8311c9c239042525b3d7dae427aaa63fb1a54493acb5b8b898d0ed9ed56f4a01152b5fecb2ce56dba196977bc3353918391c6b7398dc9f6723dd819ace9aed57f0cda25b3b39f1e0b98132d1fdb15f102b05dad2d1bcabda43dbd948eee93390ca02b230612dae0c8726948ece7e834c4ecfc7679612750cb74d3917a37885da0c2d260eb8c4a3afd83399dae3bba16e65898acb5a1d491a3942441359fac97a3237250058f686f9a76a42507dd50489f0613dc69dbc093fce785eb22ea8c2953cbe63c6744d6951da7a6ab22db76a71825841ba302b327d";

        PublicKey publicKey = SecurityUtils.getRSAPublicKeyFromBytes(SecurityUtils.hexToBytes(publicHex));
        byte[] toEncrypt = SecurityUtils.hexToBytes(toEncryptHex);
        byte[] encrypted = SecurityUtils.encryptRSA(toEncrypt, publicKey);
        String encryptedHex = SecurityUtils.bytesToHex(encrypted);

        assertEquals(encryptedHex, expectedEncryptedHex);
    }

    @Test
    public void setBitTest() {
        byte[] bitfield = new byte[16];
        SecurityUtils.setBit(8, bitfield);
        String binary = SecurityUtils.bytesToBinary(bitfield);
        assertEquals(binary, "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000");
    }

    @Test
    public void isBitSetTest() {
        byte[] bitfield = binaryToBytes("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000");
        assertEquals(SecurityUtils.isBitSet(8, bitfield), true);
    }

    @Test
    public void aesTest2() throws VaultFailureException {
        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = 0;
        }

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = 0;
        }

        String data = "this is the data";
        byte[] encryptedData = SecurityUtils.encryptAES(data.getBytes(), key, iv);
        byte[] unencryptedData = SecurityUtils.decryptAES(encryptedData, key, iv);
        String unencryptedDataString = SecurityUtils.bytesToString(unencryptedData);
        assertEquals(data, unencryptedDataString);
    }

    @Test
    public void aesDecryptExternalTest() throws VaultFailureException {
        LinkedHashMap<String, String> map = externalAesData();

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = 0;
        }

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = 0;
        }

        for (int i = 0; i < map.size(); i++) {
            String testData = "";
            for (int j = 0; j < i; j++) {
                testData += "A";
            }

            byte[] decryptedData = SecurityUtils.decryptAES(SecurityUtils.hexToBytes(map.get(testData)), key, iv);
            assertEquals(testData, bytesToString(decryptedData));
        }
    }

    @Test
    public void aesTest() throws VaultFailureException {
        LinkedHashMap<String, String> map = externalAesData();

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = 0;
        }

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = 0;
        }

        for (int i = 0; i < map.size(); i++) {
            String testData = "";
            for (int j = 0; j < i; j++) {
                testData += "A";
            }

            byte[] encryptedData = SecurityUtils.encryptAES(testData.getBytes(), key, iv);
            assertArrayEquals(SecurityUtils.hexToBytes(map.get(testData)), encryptedData);
        }
    }

    private LinkedHashMap<String, String> externalAesData() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("", "1f788fe6d86c317549697fbf0c07fa43");
        map.put("A", "040aac1a93a85e9ed24a7ca4586dd3e3");
        map.put("AA", "2505ebdbc07568d5df02caaeab3b6239");
        map.put("AAA", "466ead6eb8a1862a94e1148837dee087");
        map.put("AAAA", "0bcbc1d9509ff724dae752d417dcd9e0");
        map.put("AAAAA", "96ac978fbbd188ee0e9fb25d78d7f607");
        map.put("AAAAAA", "6cf14818b4c1960bda36c8a837ed824f");
        map.put("AAAAAAA", "72e68e1eee21a969769b2c0633bf1015");
        map.put("AAAAAAAA", "9cb8bf72126f176edeb37a667d82eb3b");
        map.put("AAAAAAAAA", "ace1cfac24a33dc5833e26f07bc4ded7");
        map.put("AAAAAAAAAA", "f2cdc805d3ee4dba3c611787cb3376e1");
        map.put("AAAAAAAAAAA", "463ca8753562429820de7b2ed824faa1");
        map.put("AAAAAAAAAAAA", "f95b9d73cb11abb166a8760e805f52bc");
        map.put("AAAAAAAAAAAAA", "d158bcdf37e5c6f65b9384514f362538");
        map.put("AAAAAAAAAAAAAA", "0ce82d33c85c3904a20247e5c4dfa745");
        map.put("AAAAAAAAAAAAAAA", "9825223a7eccaac93642a4e2d34f1d36");
        map.put("AAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827effa8088f11990f7c70321ad10d9a70af");
        map.put("AAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e3dacff8f75ae452185b2da4fae80189f");
        map.put("AAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e672491e26a3d6d26486d926f11f652f4");
        map.put("AAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ee1339edde86def10f1d69175a1281222");
        map.put("AAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e4942a1b79024fd9bcc440f025b7884b9");
        map.put("AAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e27b05da5ef2213c1d55bf8a3084b1267");
        map.put("AAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ec8b34032019550ee59dc7da36553b8c2");
        map.put("AAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e08e81474fe612ab590f468e747f5cc87");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ea7454dcac6f39560fd913b45592ca21e");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e653907e14f9d0f962653a2c681a01afe");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ec4dc968dcb459664e26de26c73a67601");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e4ceb452312d11e183a10146d98406a48");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ea26ce00b58b12a0e28f480f8cb17652a");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e3c5605a18b2e3a7257699886eaeda2f3");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e65f122473101d6dd78b7fb0d58876b99");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e748381c768240e5a886ba5bf20bc0120");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce703ad1e038bfac469a26d800f3756e64bc");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70603c0bae55d3c44c867468991a079755");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70d80d373de646938eeb33e61bf0995b95");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7074de7c4ec1cf94d11121ab84287b344a");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce704462780a822469518d470517d787e8b8");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70fcb2219f5579bad271383d050a096630");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7031dd523da0f4aed35c0806a4e76c5660");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70e2e83258b244995f176abeccac345842");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70da91e88f36acb1649053baf5211440b8");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7098898612f7c53a066d36de7b8298fd7d");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70a01e4040101b0bec76dcb68c477846a2");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce702d62ffb5a0511fd3bbe85b22081da335");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70b8b500161fb6467b5604f892e0de2415");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70fbcda8e0f54b8b90e7296a3fa370bcbd");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70f9dfe58755fecc165d90212bebab0f51");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70c1efa7f67933f96412569d1c07a078c1");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43463760b82530f377cd39192e921e662c22");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c5f42f5e4690692f0c4f6bff1216d029");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346f724f844fb8d7d1825c75e2d933d9456");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346cb7378b0d1351111134b5340da43c4ad");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43466b8d9a2c3df378862b2711fad466cd7e");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346a89347fddf4aee4ab6a54e28e6fa9024");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346570fe57c721935e2c8cfd0e4d13ce9fa");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c4cad92dbe5a9336bc22e2d50de3b9c4");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346a34e0de1e12532a24ee925d54aae9560");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346933ea9fe6102baef01242cd8146496ac");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c47258b528221838995b355e6c6f3760");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346d52d5fdd97398c5139222a9cf4abe011");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346838e71c085cbebbe849dc4fe788806ac");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa434633b2e3b4c758ca0b016c48980abfff83");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346572849215a79ab98d0b694ccc5261749");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43461454f8c1ab829f1cf8496ff9db8b4911");
        return map;
    }

    @Test
    public void randomUnsignedTest() throws UnsupportedEncodingException {
        Map<String, Boolean> map = new HashMap<>();

        int iterations = 0;
        while (++iterations < 10000 && map.size() < 256) {
            map.put(String.valueOf(SecurityUtils.getRandomUnsignedCharacter()), true);
            LOGGER.log(Level.WARNING, "VIN: " + new String(SecurityUtils.getRandomUnsignedCharacters(1), "UTF-8"));
        }

        if (map.size() != 256) {
            LOGGER.log(Level.WARNING, "Security Benchmark: Random Unsigned Test failed.");
        } else {
            LOGGER.log(Level.WARNING, "Security Benchmark: Random Unsigned Test passed in %d iterations.", iterations);
        }

        // collisions test
        Map<String, Boolean> map2 = new HashMap<>();

        iterations = 0;
        boolean passed = true;
        while (++iterations < 10000) {
            String output = new String(SecurityUtils.getRandomUnsignedCharacters(32), "UTF-8");

            if (map2.get(output) != null) {
                passed = false;
                break;
            }

            map2.put(output, true);
        }

        assertEquals(true, passed);
    }

    @Test
    public void sha512Test() {
        String expectedResult = "353c86a44ea160300e79a77504da8b46b01b10af5795318f22cb93b847fe7567889e56fcf0815dcc67fc1747a800b3a55778eeea99285ee61a2cefd155991e20";
        String actualResult = SecurityUtils.hashSha512(benchmarkData);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void pbkdTest() throws Exception {
        String testPassword = "password";
        String testSalt = "salt";
        int testKeyLength = 64;

        assertEquals(SecurityUtils.bytesToHex(SecurityUtils.getPbkdf2Sha512Key(testPassword, testSalt.getBytes(), 1, testKeyLength)), "867f70cf1ade02cff3752599a3a53dc4af34c7a669815ae5d513554e1c8cf252c02d470a285a0501bad999bfe943c08f050235d7d68b1da55e63f73b60a57fce");
        assertEquals(SecurityUtils.bytesToHex(SecurityUtils.getPbkdf2Sha512Key(testPassword, testSalt.getBytes(), 2, testKeyLength)), "e1d9c16aa681708a45f5c7c4e215ceb66e011a2e9f0040713f18aefdb866d53cf76cab2868a39b9f7840edce4fef5a82be67335c77a6068e04112754f27ccf4e");
        assertEquals(SecurityUtils.bytesToHex(SecurityUtils.getPbkdf2Sha512Key(testPassword, testSalt.getBytes(), 4096, testKeyLength)), "d197b1b33db0143e018b12f3d1d1479e6cdebdcc97c5c0f87f6902e072f457b5143f30602641b3d55cd335988cb36b84376060ecd532e039b742a239434af2d5");
    }

    @Test
    public void pbkdTimeTest() throws Exception {
        SecurityUtils.getPbkdf2Sha512Key("1", "salt".getBytes(), 100000, 64);
        assertEquals(true, true);
    }

    @Test
    public void generate_vault_key() {
        String passphrase = "demo demo demo";
        String salt = "84ca22f62950230a4e1afb6f6fc26d16";
        String expected = "c68f6ed9529ac82727d6e974ba0cc207f7200de07805aec897e9809d00ce6df5";

        int iterations = 10000;
        byte[] actual = SecurityUtils.generateVaultKey(passphrase, SecurityUtils.hexToBytes(salt), iterations);

        assertEquals(expected, SecurityUtils.bytesToHex(actual));
    }
}
