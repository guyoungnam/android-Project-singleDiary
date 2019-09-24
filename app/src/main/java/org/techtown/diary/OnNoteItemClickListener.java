package org.techtown.diary;

import android.view.View;

interface OnNoteItemClickListener {

    public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position);
}
