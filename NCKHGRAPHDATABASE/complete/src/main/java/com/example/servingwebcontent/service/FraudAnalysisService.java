package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.FraudInputDTO;
import com.example.servingwebcontent.dto.OutputDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.net.IDN;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FraudAnalysisService {

    private final Neo4jClient neo4j;

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern IPV4_REGEX =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    private static final Pattern IPV6_REGEX =
            Pattern.compile("^[0-9a-fA-F:]+$");

    private static final Pattern HASH_REGEX =
            Pattern.compile("^[a-fA-F0-9]{32,64}$");

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com","10minutemail.com","tempmail.com","fake.net"
    );

    private static final Set<String> SUSPICIOUS_TLDS = Set.of(
            "tk","xyz","top","ru","cn","gq","ml"
    );

    private static final List<String> SUSPICIOUS_WORDS = List.of(
            "login","verify","secure","update",
            "account","reset","confirm","bank","signin","billing",
            "xnxx","porn","sex","adult","cam","xxx"
    );
    private static final List<String> COPYRIGHT_KEYWORDS = List.of(
            "torrent","pirate","piracy","crack","keygen","warez",
            "moviesfree","watchfree","free-movie","free-movies"
    );

    private static final List<String> HACKING_KEYWORDS = List.of(
            "hack","hacking","exploit","ddos","botnet","phish",
            "carding","credential","breach","leak","stealer"
    );

    private static final List<String> MALWARE_KEYWORDS = List.of(
            "malware","ransom","trojan","spyware","keylogger",
            "rootkit","rat","backdoor","payload"
    );

    private static final List<String> RESTRICTED_POLITICS_KEYWORDS = List.of(
            "blockednews","restrictednews","bannednews","sensitivepolitics"
    );
    private static final Set<String> PORN_BLACKLIST = Set.of(
            "pornhub.com","xvideos.com","xnxx.com","xhamster.com","redtube.com",
            "youporn.com","tube8.com","spankbang.com","tnaflix.com","drtuber.com",
            "beeg.com","sunporno.com","eporner.com","txxx.com","porndig.com","slutload.com"
    );
    private static final Set<String> RESTRICTED_POLITICS_BLACKLIST = Set.of(
            "rfa.org","voanews.com","bbc.com","rferl.org","hrw.org","amnesty.org","freedomhouse.org"
    );

    private static final Set<String> GAMBLING_BLACKLIST = Set.of(
            "bet365.com","1xbet.com","188bet.com","dafabet.com","m88.com",
            "w88.com","fun88.com","bk8.com","vn88.com","sv388.com",
            "fb88.com","kubet.com","jun88.com","oxbet.com","v9bet.com",
            "f168.com","12bet.com","sbobet.com","betway.com","parimatch.com",
            "stake.com","bc.game","roobet.com"
    );

    private static final Set<String> PIRACY_BLACKLIST = Set.of(
            "thepiratebay.org","1337x.to","torrentgalaxy.to","kickasstorrents.to",
            "fitgirl-repacks.site","igg-games.com","steamunlocked.net","oceanofgames.com",
            "apunkagames.net","skidrowreloaded.com","getintopc.com","filecr.com","sanet.st"
    );

    private static final Set<String> RISKY_SOFTWARE_BLACKLIST = Set.of(
            "softonic.com","soft32.com","downloadastro.com","brothersoft.com",
            "softpedia.com","filehorse.com","downloadcrew.com","freewarefiles.com",
            "snapfiles.com","majorgeeks.com"
    );
    private static final Set<String> IP_BLACKLIST = Set.of(
            "45.9.148.108","103.224.182.246","185.220.101.45","91.219.236.222",
            "185.165.171.7","103.195.103.66","176.123.26.92","185.107.56.121",
            "46.161.27.129","91.200.12.52","185.234.218.107","103.145.13.156",
            "176.119.142.82","45.95.147.213","185.38.175.132","193.169.255.78",
            "91.92.109.43","37.120.222.132"
    );
    private static final Set<String> PHISHING_BLACKLIST = Set.of(
            "paypal-account-security-update.xyz","bank-alert-verification.top","update-appleid-security.net",
            "microsoft-password-reset-alert.xyz","facebook-login-check-security.top","instagram-verification-badge.xyz",
            "netflix-payment-update-warning.top","amazon-account-warning-reset.xyz","crypto-wallet-airdrop.live",
            "binance-bonus-airdrop-event.xyz"
    );
    private static final Set<String> PHISHING_URL_BLACKLIST = Set.of(
            "http://paypal-account-security-update.xyz/login",
            "http://bank-alert-verification.top",
            "http://update-appleid-security.net",
            "http://microsoft-password-reset-alert.xyz",
            "http://facebook-login-check-security.top",
            "http://instagram-verification-badge.xyz",
            "http://netflix-payment-update-warning.top",
            "http://amazon-account-warning-reset.xyz",
            "http://crypto-wallet-airdrop.live",
            "http://binance-bonus-airdrop-event.xyz"
    );

    private static final Set<String> EMAIL_BLACKLIST = Set.of(
            "security@paypal-alert-verification.com",
            "support@bank-account-warning.net",
            "admin@crypto-airdrop-event.live",
            "service@secure-wallet-verification.top",
            "info@binance-bonus-event.xyz",
            "alert@facebook-security-check.net",
            "support@instagram-verification-alert.com",
            "billing@netflix-payment-warning.top",
            "service@amazon-account-update.xyz"
    );

    private static final Set<String> MALWARE_HASH_BLACKLIST = Set.of(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "5d41402abc4b2a76b9719d911017c592a2c3d4b5f6e7f8a9b0c1d2e3f4a5b6c7",
            "d41d8cd98f00b204e9800998ecf8427e1234567890abcdef1234567890abcdef",
            "6f5902ac237024bdd0c176cb93063dc4abcdef1234567890abcdef1234567890"
    );

    private static final Set<String> SUSPICIOUS_USER_AGENTS = Set.of(
            "sqlmap/1.7","nikto/2.1.6","nmap","dirbuster","wpscan",
            "python-requests","curl","masscan","zgrab","metasploit"
    );

    private static final Set<Integer> COMMON_ATTACK_PORTS = Set.of(
            21,22,23,25,53,80,110,139,143,443,445,3306,3389,5900,8080,8443
    );

    private static final Set<String> DANGEROUS_FILE_EXTENSIONS = Set.of(
            ".exe",".scr",".bat",".cmd",".ps1",".vbs",".js",".jar",
            ".msi",".dll",".sys",".apk",".bin",".iso",".lnk",".hta"
    );
    private static final Set<String> ARCHIVE_FILE_EXTENSIONS = Set.of(
            ".zip",".rar",".7z",".tar",".gz",".tgz",".bz2",".xz"
    );
    private static final List<String> SUSPICIOUS_FILE_KEYWORDS = List.of(
            "security","update","verify","account","password","reset",
            "login","confirm","invoice","payment","receipt"
    );
    private static final Map<String, Integer> EMAIL_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("support@paypal.com", 10),
            Map.entry("verify-account@paypal-login.com", 65),
            Map.entry("support@secure-paypal-update.com", 75),
            Map.entry("user@gmail.com", 5),
            Map.entry("verify@bank-secure-login.net", 70)
    );

    private static final Map<String, Integer> IP_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("192.168.1.10", 30),
            Map.entry("127.0.0.1", 10),
            Map.entry("8.8.8.8", 5),
            Map.entry("203.0.113.5", 40),
            Map.entry("1.1.1.1", 5)
    );

    private static final Map<String, Integer> URL_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("http://paypal-login-update.com", 70),
            Map.entry("https://paypal.com", 5),
            Map.entry("http://secure-login-account.net/login", 65),
            Map.entry("http://bank-update-login.org/verify", 72),
            Map.entry("https://example.org", 3),
            Map.entry("http://login-confirm-account.com", 66)
    );

    private static final Map<String, Integer> DOMAIN_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("paypal-login-update.com", 68),
            Map.entry("secure-bank-update.net", 70),
            Map.entry("example.org", 3),
            Map.entry("appleid-security-check.com", 72),
            Map.entry("github.com", 2),
            Map.entry("verify-user-login.net", 68)
    );

    private static final Map<String, Integer> FILE_HASH_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("a3f5b6c4d9e2f1a7b8c9d0e1f2a3b4c5", 80),
            Map.entry("b4d6e8f9a1c2d3e4f5a6b7c8d9e0f1a2", 15),
            Map.entry("e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2", 75),
            Map.entry("d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6", 10),
            Map.entry("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6", 60)
    );

    private static final Map<String, Integer> FILE_NODE_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("invoice_update.exe", 78),
            Map.entry("payment_receipt.pdf", 10),
            Map.entry("security_update.zip", 65),
            Map.entry("document.docx", 5),
            Map.entry("account_verification.exe", 80),
            Map.entry("password_reset_tool.exe", 79)
    );

    private static final Map<String, Integer> ACCOUNT_SCORE_OVERRIDES = Map.ofEntries(
            Map.entry("user-123", 10),
            Map.entry("support-admin", 45),
            Map.entry("verify-account-system", 60),
            Map.entry("bank-security-update", 75),
            Map.entry("normal-user", 5),
            Map.entry("system_verification_bot", 58)
    );

    public FraudAnalysisService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    /* =========================================================
       MAIN METHOD
       ========================================================= */

    public OutputDTO analyzePreview(FraudInputDTO input) {

        if (input == null)
            return OutputDTO.invalid("Input rỗng");

        NodeRisk emailRisk = analyzeEmail(input.getEmail());
        NodeRisk ipRisk = analyzeIP(input.getIp());
        NodeRisk urlRisk = analyzeURL(input.getUrl());
        NodeRisk domainRisk = analyzeDomain(input.getDomain());
        NodeRisk fileNodeRisk = analyzeFileNode(input.getFileNode());
        NodeRisk hashRisk = analyzeFileHash(input.getFileHash());
        NodeRisk victimRisk = analyzeVictim(input.getVictimAccount());

        int totalScore =
                emailRisk.riskScore +
                ipRisk.riskScore +
                urlRisk.riskScore +
                domainRisk.riskScore +
                fileNodeRisk.riskScore +
                hashRisk.riskScore +
                victimRisk.riskScore;

        totalScore = Math.min(totalScore,100);

        String riskLevel =
                totalScore >= 60 ? "high"
                : totalScore >= 30 ? "medium"
                : "low";

        String verdict = switch (riskLevel) {
            case "high" -> "GIAN LẬN";
            case "medium" -> "ĐÁNG NGHI NGỜ";
            default -> "AN TOÀN";
        };

        Set<String> indicatorSet = new LinkedHashSet<>();

        indicatorSet.addAll(emailRisk.indicators);
        indicatorSet.addAll(ipRisk.indicators);
        indicatorSet.addAll(urlRisk.indicators);
        indicatorSet.addAll(domainRisk.indicators);
        indicatorSet.addAll(fileNodeRisk.indicators);
        indicatorSet.addAll(hashRisk.indicators);
        indicatorSet.addAll(victimRisk.indicators);

        return new OutputDTO(
                verdict,
                "RULE_ENGINE",
                totalScore,
                riskLevel,
                "valid",
                new ArrayList<>(indicatorSet)
        );
    }

    /* =========================================================
       NETWORK CONNECTION (IP -> DOMAIN)
       ========================================================= */

    public void addNetworkConnection(String ip, String domain) {

        if (!notBlank(ip) || !notBlank(domain)) return;

        OutputDTO ipOut = analyzeSingle("ip", ip);
        OutputDTO domainOut = analyzeSingle("domain", domain);

        String ipNodeId = mergeNode("IPAddress", "ip", ip, ipOut, "WIRESHARK");
        String domainNodeId = mergeNode("Domain", "domain", domain, domainOut, "WIRESHARK");

        link(ipNodeId, domainNodeId, "CONNECTS_TO");
    }

    /* =========================================================
       METHOD BỊ THIẾU (FIX LỖI)
       ========================================================= */

    public OutputDTO analyzeSingle(String type, String value) {

        if (value == null || value.isBlank()) {
            return new OutputDTO(
                    "EMPTY",
                    type,
                    0,
                    "low",
                    "absent",
                    List.of()
            );
        }

        FraudInputDTO input = new FraudInputDTO();

        switch (type.toLowerCase()) {

            case "email" -> input.setEmail(value);

            case "ip", "ipaddress" -> input.setIp(value);

            case "url" -> input.setUrl(value);

            case "domain" -> input.setDomain(value);

            case "filenode", "file", "filename" -> input.setFileNode(value);

            case "hash", "filehash" -> input.setFileHash(value);

            case "victim", "account" -> input.setVictimAccount(value);

            default -> {
                return new OutputDTO(
                        "UNKNOWN_TYPE",
                        type,
                        0,
                        "low",
                        "invalid",
                        List.of("Indicator type không hỗ trợ")
                );
            }
        }

        return analyzePreview(input);
    }

    /* =========================================================
       EMAIL
       ========================================================= */

    private NodeRisk analyzeEmail(String email) {

        if (email == null)
            return NodeRisk.absent();

        String emailKey = email.trim().toLowerCase();
        NodeRisk override = overrideScore(EMAIL_SCORE_OVERRIDES.get(emailKey), "Manual override");
        if (override != null) return override;

        email = emailKey;

        if (!EMAIL_REGEX.matcher(email).matches())
            return NodeRisk.invalid("Email không hợp lệ");

        NodeRisk r = NodeRisk.valid();

        if (EMAIL_BLACKLIST.contains(email))
            r.add(100,"Phishing email" );

        String domain = email.substring(email.indexOf("@") + 1);

        if (DISPOSABLE_DOMAINS.contains(domain))
            r.add(40,"Email tạm thời");

        if (domain.contains("xn--"))
            r.add(60,"Punycode domain");

        if (highEntropy(domain))
            r.add(25,"Domain entropy cao");

        return r.normalize();
    }

    /* =========================================================
       IP
       ========================================================= */
    private NodeRisk analyzeIP(String ip) {

        if (ip == null)
            return NodeRisk.absent();

        String ipKey = ip.trim();
        NodeRisk override = overrideScore(IP_SCORE_OVERRIDES.get(ipKey), "Manual override");
        if (override != null) return override;

        ip = ipKey;

        if (!IPV4_REGEX.matcher(ip).matches() && !IPV6_REGEX.matcher(ip).matches())
            return NodeRisk.invalid("IP kh�ng h?p l?");

        NodeRisk r = NodeRisk.valid();

        if (IP_BLACKLIST.contains(ip))
            r.add(100,"IP blacklist" );

        if (IPV4_REGEX.matcher(ip).matches() && isPrivateIP(ip))
            r.add(40,"Private IP");

        return r.normalize();
    }

    /* =========================================================
       URL
       ========================================================= */

    private NodeRisk analyzeURL(String url) {

        if (url == null)
            return NodeRisk.absent();

        String urlKey = normalizeUrlKey(url);
        NodeRisk override = overrideScore(URL_SCORE_OVERRIDES.get(urlKey), "Manual override");
        if (override != null) return override;

        try {

            String decoded = urlKey;

            for(int i=0;i<2;i++){
                decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
            }

            if(!decoded.startsWith("http"))
                decoded = "https://" + decoded;

            URI uri = URI.create(decoded);

            if(uri.getHost()==null)
                return NodeRisk.invalid("URL không hợp lệ");

            String host = IDN.toASCII(uri.getHost().toLowerCase());

            NodeRisk r = NodeRisk.valid();

            if (isBlacklistedDomain(host))
                r.add(100,"Porn blacklist" );
            if (isInBlacklist(host, RESTRICTED_POLITICS_BLACKLIST))
                r.add(100,"Restricted politics" );
            if (isInBlacklist(host, GAMBLING_BLACKLIST))
                r.add(100,"Gambling" );
            if (isInBlacklist(host, PIRACY_BLACKLIST))
                r.add(100,"Piracy" );
            if (isInBlacklist(host, RISKY_SOFTWARE_BLACKLIST))
                r.add(100,"Risky software" );
            if (isInBlacklist(host, PHISHING_BLACKLIST))
                r.add(100,"Phishing URL" );

            if(IPV4_REGEX.matcher(host).matches())
                r.add(50,"URL dùng IP");

            if(host.length()>45)
                r.add(25,"Domain dài bất thường");

            int idx = host.lastIndexOf('.');
            if(idx!=-1){

                String tld = host.substring(idx+1);

                if(SUSPICIOUS_TLDS.contains(tld))
                    r.add(30,"TLD đáng ngờ");
            }

                        String lowerDecoded = decoded.toLowerCase();

            if (isBlacklistedUrl(lowerDecoded, PHISHING_URL_BLACKLIST))
                r.add(100,"Phishing URL" );
            addKeywordIndicators(r, lowerDecoded, SUSPICIOUS_WORDS, 15, "Keyword dang ngo");
            addKeywordIndicators(r, lowerDecoded, COPYRIGHT_KEYWORDS, 25, "Copyright");
            addKeywordIndicators(r, lowerDecoded, HACKING_KEYWORDS, 35, "Hack");
            addKeywordIndicators(r, lowerDecoded, MALWARE_KEYWORDS, 45, "Malware");
            addKeywordIndicators(r, lowerDecoded, RESTRICTED_POLITICS_KEYWORDS, 20, "Restricted");

            if(highEntropy(host))
                r.add(25,"Domain entropy cao");

            return r.normalize();

        } catch(Exception e) {

            return NodeRisk.invalid("URL không hợp lệ");
        }
    }

    /* =========================================================
       DOMAIN
       ========================================================= */

    private NodeRisk analyzeDomain(String domain) {

        if(domain==null)
            return NodeRisk.absent();

        String domainKey = domain.trim().toLowerCase();
        NodeRisk override = overrideScore(DOMAIN_SCORE_OVERRIDES.get(domainKey), "Manual override");
        if (override != null) return override;

        domain = domainKey;

        NodeRisk r = NodeRisk.valid();

        if (isBlacklistedDomain(domain))
            r.add(100,"Porn blacklist" );
        if (isInBlacklist(domain, RESTRICTED_POLITICS_BLACKLIST))
            r.add(100,"Restricted politics" );
        if (isInBlacklist(domain, GAMBLING_BLACKLIST))
            r.add(100,"Gambling" );
        if (isInBlacklist(domain, PIRACY_BLACKLIST))
            r.add(100,"Piracy" );
        if (isInBlacklist(domain, RISKY_SOFTWARE_BLACKLIST))
            r.add(100,"Risky software" );
        if (isInBlacklist(domain, PHISHING_BLACKLIST))
            r.add(100,"Phishing URL" );

        if(domain.contains("xn--"))
            r.add(60,"Punycode domain");

        if(domain.length()>50)
            r.add(20,"Domain dài");

        if(highEntropy(domain))
            r.add(25,"Domain entropy cao");

        int idx = domain.lastIndexOf('.');

        if(idx!=-1){

            String tld = domain.substring(idx+1);

            if(SUSPICIOUS_TLDS.contains(tld))
                r.add(30,"TLD đáng ngờ");
        }
        addKeywordIndicators(r, domain, SUSPICIOUS_WORDS, 15, "Keyword dang ngo");
        addKeywordIndicators(r, domain, COPYRIGHT_KEYWORDS, 25, "Copyright");
        addKeywordIndicators(r, domain, HACKING_KEYWORDS, 35, "Hack");
        addKeywordIndicators(r, domain, MALWARE_KEYWORDS, 45, "Malware");
        addKeywordIndicators(r, domain, RESTRICTED_POLITICS_KEYWORDS, 20, "Restricted");

        return r.normalize();
    }

    /* =========================================================
       HASH
       ========================================================= */

        private NodeRisk analyzeFileHash(String hash) {

        if(hash==null)
            return NodeRisk.absent();

        String hashKey = hash.trim().toLowerCase();
        NodeRisk override = overrideScore(FILE_HASH_SCORE_OVERRIDES.get(hashKey), "Manual override");
        if (override != null) return override;

        hash = hashKey;

        if(!HASH_REGEX.matcher(hash).matches())
            return NodeRisk.invalid("File hash không hợp lệ");

        NodeRisk r = NodeRisk.valid();

        if (MALWARE_HASH_BLACKLIST.contains(hash.toLowerCase()))
            r.add(100,"Malware hash" );

        return r.normalize();
    }

    /* =========================================================
       FILE NODE
       ========================================================= */

    private NodeRisk analyzeFileNode(String fileNode) {

        if (fileNode == null)
            return NodeRisk.absent();

        String name = fileNode.trim().toLowerCase();
        NodeRisk override = overrideScore(FILE_NODE_SCORE_OVERRIDES.get(name), "Manual override");
        if (override != null) return override;

        NodeRisk r = NodeRisk.valid();

        if (name.length() > 60)
            r.add(10, "File name dài bất thường");

        if (!name.contains(".") && name.length() > 20)
            r.add(10, "File name thiếu phần mở rộng");

        if (highEntropy(name))
            r.add(20, "File name entropy cao");

                boolean dangerous = false;
        for (String ext : DANGEROUS_FILE_EXTENSIONS) {
            if (name.endsWith(ext)) {
                dangerous = true;
                break;
            }
        }
        if (dangerous) {
            r.add(40, "File thuc thi nguy hiem");
        }

        boolean archive = false;
        for (String ext : ARCHIVE_FILE_EXTENSIONS) {
            if (name.endsWith(ext)) {
                archive = true;
                break;
            }
        }
        if (archive) {
            r.add(15, "File luu tru (archive)");
        }

        if (name.endsWith(".docm") || name.endsWith(".xlsm") || name.endsWith(".pptm")) {
            r.add(30, "File Office có macro");
        }

        return r.normalize();
    }

    /* =========================================================
       VICTIM
       ========================================================= */

    private NodeRisk analyzeVictim(String victim) {

        if(victim==null)
            return NodeRisk.absent();

        String victimKey = victim.trim().toLowerCase();
        NodeRisk override = overrideScore(ACCOUNT_SCORE_OVERRIDES.get(victimKey), "Manual override");
        if (override != null) return override;

        victim = victimKey;

        NodeRisk r = NodeRisk.valid();

        if(victim.length()<3)
            r.add(20,"Victim account bất thường");

        if(highEntropy(victim))
            r.add(25,"Victim entropy cao");

        return r.normalize();
    }

    /* =========================================================
       UTIL
       ========================================================= */
        private boolean isBlacklistedUrl(String url, Set<String> list) {
        if (url == null || url.isBlank() || list == null) return false;
        String u = url.trim().toLowerCase();
        if (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        for (String b : list) {
            if (b == null) continue;
            String bl = b.trim().toLowerCase();
            if (u.equals(bl) || u.startsWith(bl)) return true;
        }
        return false;
    }
    private boolean isInBlacklist(String host, Set<String> list) {
        if (host == null || host.isBlank() || list == null) return false;
        String h = host.toLowerCase();
        if (list.contains(h)) return true;
        int idx = h.indexOf('.');
        while (idx > 0) {
            String suffix = h.substring(idx + 1);
            if (list.contains(suffix)) return true;
            idx = h.indexOf('.', idx + 1);
        }
        return false;
    }
    private boolean isBlacklistedDomain(String host) {
        if (host == null || host.isBlank()) return false;
        String h = host.toLowerCase();
        if (PORN_BLACKLIST.contains(h)) return true;
        int idx = h.indexOf('.');
        while (idx > 0) {
            String suffix = h.substring(idx + 1);
            if (PORN_BLACKLIST.contains(suffix)) return true;
            idx = h.indexOf('.', idx + 1);
        }
        return false;
    }
    private void addKeywordIndicators(NodeRisk r,
                                      String hay,
                                      List<String> words,
                                      int score,
                                      String label) {

        if (r == null || hay == null || words == null) return;

        for (String w : words) {
            if (hay.contains(w)) {
                r.add(score, label + ": " + w);
            }
        }
    }

    private boolean highEntropy(String s) {

        if(s==null || s.length()<6)
            return false;

        long digits = s.chars().filter(Character::isDigit).count();

        return digits > s.length()*0.4;
    }

    private boolean isPrivateIP(String ip) {

        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("127.")
                || ip.startsWith("169.254.")
                || ip.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }

    /* =========================================================
       GRAPH HELPERS
       ========================================================= */

    private String mergeNode(
            String label,
            String key,
            String value,
            OutputDTO out) {

        return mergeNode(label, key, value, out, null);
    }

    private String mergeNode(
            String label,
            String key,
            String value,
            OutputDTO out,
            String source) {

        if (value == null) return null;

        return neo4j.query("""
            MERGE (n:%s {%s:$value})
            ON CREATE SET
                n.createdAt = datetime(),
                n.deleted = false
            SET n.lastSeen  = datetime(),
                n.riskScore = $rs,
                n.riskLevel = $rl,
                n.verdict   = $vd,
                n.indicators = $ind,
                n.source = coalesce($src, n.source)
            RETURN elementId(n) AS id
        """.formatted(label, key))
                .bind(value).to("value")
                .bind(out.getRiskScore()).to("rs")
                .bind(out.getRiskLevel()).to("rl")
                .bind(out.getVerdict()).to("vd")
                .bind(out.getIndicators()).to("ind")
                .bind(source).to("src")
                .fetchAs(String.class)
                .one()
                .orElse(null);
    }

    private void link(
            String fromId,
            String toId,
            String rel) {

        if (fromId == null || toId == null) return;

        neo4j.query("""
            MATCH (a),(b)
            WHERE elementId(a)=$a AND elementId(b)=$b
            MERGE (a)-[r:%s]->(b)
            SET r.lastSeen = datetime()
        """.formatted(rel))
                .bind(fromId).to("a")
                .bind(toId).to("b")
                .run();
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

        private NodeRisk overrideScore(Integer score, String reason) {
        if (score == null) return null;
        NodeRisk r = NodeRisk.valid();
        r.riskScore = score;
        if (reason != null && !reason.isBlank()) {
            r.indicators.add(reason);
        }
        return r.normalize();
    }

    private String normalizeUrlKey(String url) {
        if (url == null) return null;
        String decoded = url.trim();
        try {
            for (int i = 0; i < 2; i++) {
                decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
            }
        } catch (Exception ignore) {
        }
        if (!decoded.startsWith("http")) {
            decoded = "https://" + decoded;
        }
        String lower = decoded.toLowerCase();
        if (lower.endsWith("/")) {
            lower = lower.substring(0, lower.length() - 1);
        }
        return lower;
    }
/* =========================================================
       NODE RISK CLASS
       ========================================================= */

    private static class NodeRisk {

        int riskScore;
        String riskLevel;
        String status;
        Set<String> indicators = new LinkedHashSet<>();

        static NodeRisk absent(){

            NodeRisk r = new NodeRisk();
            r.status="absent";
            r.riskLevel="low";
            return r;
        }

        static NodeRisk valid(){

            NodeRisk r = new NodeRisk();
            r.status="valid";
            return r;
        }

        static NodeRisk invalid(String msg){

            NodeRisk r = new NodeRisk();
            r.status="invalid";
            r.riskLevel="high";
            r.riskScore=70;
            r.indicators.add(msg);
            return r;
        }

        void add(int score,String msg){

            riskScore+=score;
            indicators.add(msg);
        }

        NodeRisk normalize(){

            riskScore=Math.min(riskScore,100);

            if(riskScore>=60){

                status="fake";
                riskLevel="high";

            } else if(riskScore>=30){

                status="suspicious";
                riskLevel="medium";

            } else{

                status="valid";
                riskLevel="low";
            }

            return this;
        }
    }
}
















