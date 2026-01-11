use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jlong, jint};
use vlc::{Instance, MediaPlayer, Media};
use std::ffi::c_void;

#[repr(C)]
pub struct RustMediaPlayer {
    instance: Instance,
    media_player: MediaPlayer,
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_init(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    let instance = match Instance::new() {
        Some(instance) => instance,
        None => return 0,
    };
    let media_player = match MediaPlayer::new(&instance) {
        Some(media_player) => media_player,
        None => return 0,
    };
    let player = Box::new(RustMediaPlayer {
        instance,
        media_player,
    });
    Box::into_raw(player) as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_setSurface(
    mut env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
    surface: JObject,
) {
    if player_ptr == 0 {
        return;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    let a_native_window = unsafe { ndk_sys::ANativeWindow_fromSurface(env.get_native_interface(), surface.into_inner()) };
    player.media_player.set_android_context(a_native_window as *mut c_void);
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_loadMedia(
    mut env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
    media_path: JString,
) {
    if player_ptr == 0 {
        return;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    let path: String = match env.get_string(&media_path) {
        Ok(path) => path.into(),
        Err(_) => return,
    };
    let media = match Media::new_path(&player.instance, &path) {
        Some(media) => media,
        None => return,
    };
    player.media_player.set_media(&media);
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_addSubtitle(
    mut env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
    subtitle_path: JString,
) {
    if player_ptr == 0 {
        return;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    let path: String = match env.get_string(&subtitle_path) {
        Ok(path) => path.into(),
        Err(_) => return,
    };
    let _ = player.media_player.add_slave(vlc::SlaveType::Subtitle, &path, true);
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_getTime(
    _env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
) -> jlong {
    if player_ptr == 0 {
        return 0;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    player.media_player.get_time().unwrap_or(0)
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_play(
    _env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
) {
    if player_ptr == 0 {
        return;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    let _ = player.media_player.play();
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_pause(
    _env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
) {
    if player_ptr == 0 {
        return;
    }
    let player = unsafe { &mut *(player_ptr as *mut RustMediaPlayer) };
    player.media_player.pause();
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_superfastbrowser_RustVideoPlayerActivity_release(
    _env: JNIEnv,
    _class: JClass,
    player_ptr: jlong,
) {
    if player_ptr == 0 {
        return;
    }
    let _ = unsafe { Box::from_raw(player_ptr as *mut RustMediaPlayer) };
}
