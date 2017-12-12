package RentalcarsApplicationWithREST;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReturnSearchController {
    
    @RequestMapping("/CarScore")
    public ReturnSearch CarScore() throws Exception{
        return new ReturnSearch("Sorted by car score",SearchAndSortJSON.printCarScore());
    }

    @RequestMapping("/Price")
    public ReturnSearch Price() throws Exception{
        return new ReturnSearch("Sorted by price", SearchAndSortJSON.printNameAndPrice());
    }

    @RequestMapping("/SIPP")
    public ReturnSearch SIPP() throws Exception{
        return new ReturnSearch("Displaying sipp", SearchAndSortJSON.printSIPPEvaluation());
    }

    @RequestMapping("/CarTypeBySupplier")
    public ReturnSearch CarTypeBySupplier() throws Exception{
        return new ReturnSearch("Sorted by car type by supplier", SearchAndSortJSON.printCarTypeBySupplier());
    }
}
