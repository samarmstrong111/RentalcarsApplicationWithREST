package RentalcarsApplicationWithREST;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.json.*;

public class ReturnSearch {
    private final String id;
    private final List<String> content;

    public ReturnSearch(String id, List<String> content) throws Exception
    {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public List<String> getContent() {
        return content;
    }
}
