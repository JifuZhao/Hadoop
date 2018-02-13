/******************************************************************************
 * Output to DataBase
 *
 ******************************************************************************/

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.mapreduce.lib.db.DBWritable;

public class DBOutputWritable implements DBWritable {
    private String preWords;
    private String nextWords;
    private int count;

    public DBOutputWritable(String preWords, String nextWords, int count) {
        this.preWords = preWords;
        this.nextWords = nextWords;
        this.count = count;
    }

    public void readFields(ResultSet arg0) throws SQLException {
        this.preWords = arg0.getString(1);
        this.nextWords = arg0.getString(2);
        this.count = arg0.getInt(3);
    }

    public void write(PreparedStatement arg0) throws SQLException {
        arg0.setString(1, preWords);
        arg0.setString(2, nextWords);
        arg0.setInt(3, count);
    }
}
