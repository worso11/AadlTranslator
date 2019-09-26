package lukowicz.application.data;

public class ConnectionNode {

    private String transId;
    private String placeId;
    private String category;
    private String headId;
    private String headCategory;
    private String period;
    private Double positionX;
    private Double positionY;

    public ConnectionNode(String transId, String placeId, String category, String headId, String headCategory, String period,
                          Double positionX, Double positionY) {
        this.transId = transId;
        this.placeId = placeId;
        this.category = category;
        this.headId = headId;
        this.headCategory = headCategory;
        this.period = period;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public String getTransId() {
        return transId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getCategory() {
        return category;
    }

    public String getHeadId() {
        return headId;
    }

    public String getHeadCategory() {
        return headCategory;
    }

    public String getPeriod() {
        return period;
    }

    public Double getPositionX() {
        return positionX;
    }


    public Double getPositionY() {
        return positionY;
    }


}

