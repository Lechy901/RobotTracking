package base;

/**
 * Describes the current program stage
 * 
 * @author ALechovsky
 *
 */
public enum WindowStage {
	NONE, PAPER_SEARCH {
		@Override
		public WindowStage prev() {
			return this;
		}
	}, GRAPH_SEARCH, ROBOT_TRACKING {
		@Override
		public WindowStage next() {
			return this;
		}
	};
	
	public WindowStage next() {
		return values()[ordinal() + 1];
	}
	
	public WindowStage prev() {
		return values()[ordinal() - 1];
	}
}
