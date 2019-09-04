package org.techtown.diary;

import android.view.View;

interface OnNoteItemClickListner {

    public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position);
}
