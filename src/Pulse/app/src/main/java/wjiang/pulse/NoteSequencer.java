package wjiang.pulse;

/**
 * Created by wjiang on 2/28/14.
 */
// format: Note    0AAA BBBC, A - Height | B - Step | C - Rising Flag
//         Command 1AAA ABBB, A - Length (from 001 (1/8) to 111(8/1 with point)
//                            B - Tone (reserved)

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class NoteSequencer {

    private ArrayList<Byte> mNoteArray;
    private ArrayList<Byte> mCmdArray;

    public NoteSequencer() {
        super();

        this.mNoteArray = new ArrayList<Byte>();
        this.mCmdArray  = new ArrayList<Byte>();
    }

    public void add(Byte note, Byte cmd) {
        this.mNoteArray.add(note);
        this.mCmdArray.add(cmd);
    }

    public Byte[] poll() {
        Byte[] element = new Byte[2];

        if(this.isEmpty()) {
            return null;
        }

        try {
            element[0] = this.mNoteArray.get(0);
            element[1] = this.mCmdArray.get(0);
            this.mNoteArray.remove(0);
            this.mCmdArray.remove(0);
            return element;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isEmpty() {
        if(this.mNoteArray.isEmpty() || this.mCmdArray.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }

    public ArrayList<Byte> getNoteArray() {
        return (ArrayList<Byte>)this.mNoteArray.clone();
    }

    public ArrayList<Byte> getCmdArray() {
        return (ArrayList<Byte>)this.mCmdArray.clone();
    }

    public void readNoteFile(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("@")) {
                    reader.close();
                    break;
                }

                if(line.startsWith("#")) {
                    continue;
                }

                //System.out.println(line);
                String[] oneNote = line.split("-");
                //System.out.println(oneNote.length);
                //System.out.println(oneNote[0] + " " + oneNote[1]);
                Integer noteNumber = Integer.valueOf(oneNote[0]);
                Integer noteCmd    = Integer.valueOf(oneNote[1]);
                Byte note = (byte)(((noteNumber / 100) << 4) |
                        ((noteNumber / 10 % 10) << 1) |
                        ((noteNumber % 10)) & 0xFF);
                Byte cmd  = (byte)(((noteCmd / 100) << 4) |
                        ((noteCmd / 10 % 10) << 3) |
                        ((noteCmd % 10)) & 0xFF);
                this.add(note, cmd);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void empty() {
        this.mNoteArray.clear();
        this.mCmdArray.clear();
    }
}