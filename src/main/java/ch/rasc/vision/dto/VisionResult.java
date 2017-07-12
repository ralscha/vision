package ch.rasc.vision.dto;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import ch.rasc.vision.entity.Face;
import ch.rasc.vision.entity.Label;
import ch.rasc.vision.entity.Landmark;
import ch.rasc.vision.entity.Logo;
import ch.rasc.vision.entity.SafeSearch;
import ch.rasc.vision.entity.Text;
import ch.rasc.vision.entity.Web;

@Value.Style(jdkOnly = true)
@Value.Immutable
public interface VisionResult {

	@Nullable
	List<Label> labels();

	@Nullable
	SafeSearch safeSearch();

	@Nullable
	List<Logo> logos();

	@Nullable
	List<Landmark> landmarks();

	@Nullable
	List<Text> texts();

	@Nullable
	List<Face> faces();

	@Nullable
	Web web();
}
