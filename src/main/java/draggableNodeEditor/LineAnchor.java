package draggableNodeEditor;

/**
 * a anchor point for ConnectionStyle to consider.
 * values do not have to be normalized.
 * 
 * <br><b>NOTE! :</b><u> the exact effect of each value is determined by the ConnectionStyle using it.</u> the following are guidelines of what they should cause 
 * @pram x , y : position. 
 * @pram weight : the scale of the effect on the line. higher scale => more influence. can be negative. 
 * @pram bias : a modifier for the weight. IN and OUT are, respectively, before and after the line "passes" the anchor. 
 */
public record LineAnchor(
			int x,
			int y,
			float weight,
			float IN_biasX,
			float IN_biasY,
			float OUT_biasX,
			float OUT_biasY
		) {
	
	public float distanceTo(LineAnchor other) {
		return 0f;
	}
}
