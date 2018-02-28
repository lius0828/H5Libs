package com.ygi.h5sdk.perm;

import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * @date 2018/1/17
 */

public interface ActivityDelegate {

    void onResume();

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

    void onActivityResult(int requestCode, int resultCode, Intent data);
}
