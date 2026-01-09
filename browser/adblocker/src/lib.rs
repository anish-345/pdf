use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jboolean;
use once_cell::sync::Lazy;
use std::collections::HashSet;
use std::sync::Mutex;

// Global, thread-safe blocklist, initialized lazily.
static BLOCKLIST: Lazy<Mutex<HashSet<String>>> = Lazy::new(|| Mutex::new(HashSet::new()));

/// JNI function to load the blocklist from a string. This will be called
/// from the Kotlin side with the content of the blocklist.txt asset.
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_AdBlocker_loadBlocklist(
    mut env: JNIEnv,
    _class: JClass,
    blocklist_str: JString,
) {
    let blocklist_content = match env.get_string(&blocklist_str) {
        Ok(s) => String::from(s),
        Err(_) => {
            // In a real app, you might log this error.
            return;
        }
    };

    // Lock the mutex and update the blocklist
    let mut blocklist = BLOCKLIST.lock().unwrap();
    blocklist.clear();
    for line in blocklist_content.lines() {
        let domain = line.trim();
        if !domain.is_empty() {
            blocklist.insert(domain.to_string());
        }
    }
}

/// Checks if a given domain is in the global blocklist.
fn is_blocked(domain: &str) -> bool {
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
        Err(_) => return 0, // Return false on error
    };

    if is_blocked(&domain) {
        1
    } else {
        0
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ad_blocking_flow() {
        // Simulate loading the blocklist
        let blocklist_content = "doubleclick.net\ngoogleadservices.com";
        {
            let mut blocklist = BLOCKLIST.lock().unwrap();
            blocklist.clear();
            for line in blocklist_content.lines() {
                blocklist.insert(line.trim().to_string());
            }
        }

        // Test the is_blocked function
        assert!(is_blocked("doubleclick.net"));
        assert!(is_blocked("googleadservices.com"));
        assert!(!is_blocked("example.com"));
    }
}
