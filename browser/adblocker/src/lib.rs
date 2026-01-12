use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jboolean;
use once_cell::sync::Lazy;
use std::collections::HashSet;
use std::sync::Mutex;
use url::Url;

// Global, thread-safe blocklist, initialized lazily.
static BLOCKLIST: Lazy<Mutex<HashSet<String>>> = Lazy::new(|| Mutex::new(HashSet::new()));
static WHITELIST: Lazy<Mutex<HashSet<String>>> = Lazy::new(|| Mutex::new(HashSet::new()));

// Set of known tracking parameters.
static TRACKING_PARAMETERS: Lazy<HashSet<&'static str>> = Lazy::new(|| {
    let mut set = HashSet::new();
    set.insert("utm_source");
    set.insert("utm_medium");
    set.insert("utm_campaign");
    set.insert("utm_term");
    set.insert("utm_content");
    set.insert("fbclid");
    set.insert("gclid");
    set
});

/// JNI function to load the blocklist from a string.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_loadBlocklist(
    mut env: JNIEnv,
    _class: JClass,
    blocklist_str: JString,
) -> jboolean {
    let blocklist_content = match env.get_string(&blocklist_str) {
        Ok(s) => String::from(s),
        Err(_) => return 0,
    };

    let mut blocklist = BLOCKLIST.lock().unwrap();
    blocklist.clear();
    for line in blocklist_content.lines() {
        let domain = line.trim();
        if !domain.is_empty() {
            blocklist.insert(domain.to_string());
        }
    }
    1
}

/// JNI function to set the whitelist.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_setWhitelist(
    mut env: JNIEnv,
    _class: JClass,
    whitelist_str: JString,
) -> jboolean {
    let whitelist_content = match env.get_string(&whitelist_str) {
        Ok(s) => String::from(s),
        Err(_) => return 0,
    };

    let mut whitelist = WHITELIST.lock().unwrap();
    whitelist.clear();
    for line in whitelist_content.lines() {
        let domain = line.trim();
        if !domain.is_empty() {
            whitelist.insert(domain.to_string());
        }
    }
    1
}

/// Checks if a given domain is in the global blocklist.
fn is_blocked(domain: &str) -> bool {
    let whitelist = WHITELIST.lock().unwrap();
    if whitelist.contains(domain) {
        return false;
    }
    let blocklist = BLOCKLIST.lock().unwrap();
    blocklist.contains(domain)
}

/// JNI function to check if a domain is blocked.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_isBlocked(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jboolean {
    let domain = match env.get_string(&input) {
        Ok(s) => String::from(s),
        Err(_) => return 0,
    };

    if is_blocked(&domain) {
        1
    } else {
        0
    }
}

/// JNI function to sanitize a URL by removing tracking parameters.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_sanitizeUrl<'a>(
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    input: JString<'a>,
) -> JString<'a> {
    let url_str: String = match env.get_string(&input) {
        Ok(s) => s.into(),
        Err(_) => return input,
    };

    let mut url = match Url::parse(&url_str) {
        Ok(u) => u,
        Err(_) => return input,
    };

    let original_query_pairs: Vec<(String, String)> = url.query_pairs().map(|(k, v)| (k.into_owned(), v.into_owned())).collect();
    let mut sanitized_query_pairs = Vec::new();
    let mut trackers_removed = false;

    for (key, value) in original_query_pairs {
        if !TRACKING_PARAMETERS.contains(key.as_str()) {
            sanitized_query_pairs.push((key, value));
        } else {
            trackers_removed = true;
        }
    }

    if trackers_removed {
        url.query_pairs_mut().clear();
        for (key, value) in sanitized_query_pairs {
            url.query_pairs_mut().append_pair(&key, &value);
        }
        let sanitized_url_str = url.to_string();
        match env.new_string(sanitized_url_str) {
            Ok(s) => s.into(),
            Err(_) => input,
        }
    } else {
        input
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ad_blocking_flow() {
        let blocklist_content = "doubleclick.net\ngoogleadservices.com";
        {
            let mut blocklist = BLOCKLIST.lock().unwrap();
            blocklist.clear();
            for line in blocklist_content.lines() {
                blocklist.insert(line.trim().to_string());
            }
        }
        assert!(is_blocked("doubleclick.net"));
        assert!(!is_blocked("example.com"));
    }

    #[test]
    fn test_url_sanitization() {
        let original_url = "https://www.example.com/page?utm_source=test&param=value&fbclid=123";
        let expected_url = "https://www.example.com/page?param=value";

        // This is a direct test of the logic, not the JNI function.
        let mut url = Url::parse(original_url).unwrap();
        let original_query_pairs: Vec<(String, String)> = url.query_pairs().map(|(k, v)| (k.into_owned(), v.into_owned())).collect();
        let mut sanitized_query_pairs = Vec::new();

        for (key, value) in original_query_pairs {
            if !TRACKING_PARAMETERS.contains(key.as_str()) {
                sanitized_query_pairs.push((key, value));
            }
        }

        url.query_pairs_mut().clear();
        for (key, value) in sanitized_query_pairs {
            url.query_pairs_mut().append_pair(&key, &value);
        }

        assert_eq!(url.to_string(), expected_url);
    }
}

/// Analyzes the HTML content of a page and returns a privacy score.
pub fn analyze_page_privacy(html_content: &str) -> i32 {
    let mut score = 100;
    let lower_content = html_content.to_lowercase();

    // Deduct points for each tracker found in the blocklist.
    let blocklist = BLOCKLIST.lock().unwrap();
    for domain in blocklist.iter() {
        if lower_content.contains(domain) {
            score -= 5;
        }
    }

    // Deduct points for common fingerprinting scripts.
    if lower_content.contains("canvas.todataurl") {
        score -= 10;
    }
    if lower_content.contains("navigator.webrtc") {
        score -= 10;
    }

    // Ensure the score doesn't go below 0.
    if score < 0 {
        0
    } else {
        score
    }
}

/// JNI function to analyze a page's privacy.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_analyzePagePrivacy(
    mut env: JNIEnv,
    _class: JClass,
    html_content: JString,
) -> i32 {
    let html = match env.get_string(&html_content) {
        Ok(s) => String::from(s),
        Err(_) => return -1,
    };

    analyze_page_privacy(&html)
}
