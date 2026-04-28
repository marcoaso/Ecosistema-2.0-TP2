package simulator.model;

public interface MapInfo extends JSONable, Iterable<MapInfo.RegionData> {
    
  public record RegionData(int row, int col, RegionInfo r) {
  }

    int getCols();

    int getRows();

    int getWidth();

    int getHeight();

    int getRegionWidth();

    int getRegionHeight();
}
